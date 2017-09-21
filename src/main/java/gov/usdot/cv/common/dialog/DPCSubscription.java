package gov.usdot.cv.common.dialog;

import gov.usdot.asn1.generated.j2735.J2735;
import gov.usdot.asn1.generated.j2735.dsrc.DDateTime;
import gov.usdot.asn1.generated.j2735.dsrc.DFullTime;
import gov.usdot.asn1.generated.j2735.dsrc.Position3D;
import gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel;
import gov.usdot.asn1.generated.j2735.semi.DataSubscriptionRequest;
import gov.usdot.asn1.generated.j2735.semi.DataSubscriptionResponse;
import gov.usdot.asn1.generated.j2735.semi.GeoRegion;
import gov.usdot.asn1.generated.j2735.semi.GroupID;
import gov.usdot.asn1.generated.j2735.semi.SemiDialogID;
import gov.usdot.asn1.generated.j2735.semi.SemiSequenceID;
import gov.usdot.asn1.generated.j2735.semi.VsmType;
import gov.usdot.asn1.j2735.CVSampleMessageBuilder;
import gov.usdot.asn1.j2735.CVTypeHelper;
import gov.usdot.asn1.j2735.J2735Util;
import gov.usdot.cv.common.asn1.GroupIDHelper;
import gov.usdot.cv.common.asn1.TemporaryIDHelper;
import gov.usdot.cv.security.SecurityHelper;
import gov.usdot.cv.security.cert.CertificateException;
import gov.usdot.cv.security.cert.CertificateManager;
import gov.usdot.cv.security.cert.FileCertificateStore;
import gov.usdot.cv.security.crypto.CryptoException;
import gov.usdot.cv.security.crypto.CryptoProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import com.oss.asn1.AbstractData;
import com.oss.asn1.Coder;
import com.oss.asn1.ControlTableNotFoundException;
import com.oss.asn1.DecodeFailedException;
import com.oss.asn1.DecodeNotSupportedException;
import com.oss.asn1.EncodeFailedException;
import com.oss.asn1.EncodeNotSupportedException;

public class DPCSubscription {
	
	private static AtomicInteger requestCount = new AtomicInteger(0); 
	
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 46751;
	private static final int MAX_PACKET_SIZE = 4096;
	private static final int DEFAULT_VSM_TYPE_VALUE = 2;
	private static final int DEFAULT_END_IN_MIN = 3;
	private static final int DEFAULT_ATTEMPTS = 3;
	private static final int DEFAULT_TIMEOUT = 4000;
	private static final boolean DEFAULT_VERBOSE = true;
	private static final int DEFAULT_GROUP_ID = 0;
	
	private int requestID;
	private int subID = -1;
	private GroupID groupID = GroupIDHelper.toGroupID(0);
	
	private String replyToHost;
	private int replyToPort = DEFAULT_PORT;
	private int sendToPort = DEFAULT_PORT;
	private GeoRegion geoRegion = null;
	private int vsmTypeValue = DEFAULT_VSM_TYPE_VALUE;
	private int endInMinutes = DEFAULT_END_IN_MIN;

	private int attempts = DEFAULT_ATTEMPTS;
	private int timeout = DEFAULT_TIMEOUT;
	private boolean verbose = DEFAULT_VERBOSE;
	
	private InetAddress sendToHostAddress = null;
	
	private Coder coder = null;
	
	private boolean secureEnabled = false;
	private int psid = 0x2fe1;
	private byte[] certId8;
	private CryptoProvider cryptoProvider;
	private CertEntry[] certs;
	
	public DPCSubscription() throws DPCSubscriptionException {
		setRequestID();
		initialize();
	}
	
	public DPCSubscription(JSONObject config) throws DPCSubscriptionException {
		this();
		configure(config);
		if ( !verbose )
			coder.disableDecoderDebugging();
	}
	
	private void initialize() throws DPCSubscriptionException {
		try {
			J2735.initialize();
			coder = J2735.getPERUnalignedCoder();
			if ( verbose )
				coder.enableEncoderDebugging();
		} catch (ControlTableNotFoundException ex) {
			throw new DPCSubscriptionException("Couldn't initialize J2735 parser due to ControlTableNotFoundException", ex);
		} catch (com.oss.asn1.InitializationException ex) {
			throw new DPCSubscriptionException("Couldn't initialize J2735 parser due to com.oss.asn1.InitializationException", ex);
		}
	}
	
	public void dispose() {
		coder = null;
		J2735.deinitialize();
	}
	
	private void configure(JSONObject config) throws DPCSubscriptionException  {
		if ( config == null )
			throw new DPCSubscriptionException("Invalid parameter. Configuration file is mandatory for this constructor.");
		
		setSendToHost(config.optString("sendToHost", DEFAULT_HOST));
		
		if ( config.has("replyToHost") ) {
			setReplyToHost(config.getString("replyToHost"));
		} else {
			try {
				setReplyToHost(InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				setReplyToHost(DEFAULT_HOST);
			}
		}
		
		setSendToPort(config.optInt("sendToPort", DEFAULT_PORT));
		setReplyToPort(config.optInt("replyToPort", DEFAULT_PORT));
		setAttempts(config.optInt("attempts", DEFAULT_ATTEMPTS));
		setTimeout(config.optInt("timeout", DEFAULT_TIMEOUT));
		setVerbose(config.optBoolean("verbose", DEFAULT_VERBOSE));
		setVsmTypeValue(config.optInt("vsmTypeValue", DEFAULT_VSM_TYPE_VALUE));
		setEndInMinutes(config.optInt("endInMinutes", DEFAULT_END_IN_MIN));

		if ( config.has("serviceRegion") )
			geoRegion = getGeoRegion(config.getJSONObject("serviceRegion") );
		
		setGroupID(config.optInt("groupID", DEFAULT_GROUP_ID));
		
		setCerts(config.optJSONArray("certs"));
	}

	public void setCerts(JSONArray jsonCerts) {
		if(jsonCerts != null) {
			int count = jsonCerts.size();
			certs = new CertEntry[count];
			for( int i = 0; i < count; i++ )
				certs[i] = new CertEntry(jsonCerts.getJSONObject(i));			
		}
		else {
			certs = null;
		}
	}

	public int getGroupID() {
		return GroupIDHelper.fromGroupID(groupID);
	}
	
	public void setGroupID(int groupId) {
		groupID = GroupIDHelper.toGroupID(groupId);
	}
	
	public String getReplyToHost() {
		return replyToHost;
	}

	public void setReplyToHost(String replyToHost) {
		this.replyToHost = replyToHost;
	}

	public int getSendToPort() {
		return sendToPort;
	}

	public void setSendToPort(int sendToPort) {
		this.sendToPort = sendToPort;
	}
	
	public int getReplyToPort() {
		return replyToPort;
	}

	public void setReplyToPort(int replyToPort) {
		this.replyToPort = replyToPort;
	}

	public GeoRegion getServiceRegion() {
		return geoRegion;
	}

	public void setServiceRegion(GeoRegion geoRegion) {
		this.geoRegion = geoRegion;
	}
	
	public void setServiceRegion(double nw_lat, double nw_lon, double se_lat, double se_lon) {
		Position3D nwCnr = CVSampleMessageBuilder.getPosition3D(nw_lat, nw_lon);
		Position3D seCnr = CVSampleMessageBuilder.getPosition3D(se_lat, se_lon);
		setServiceRegion(new GeoRegion(nwCnr,seCnr));
	}

	public int getVsmTypeValue() {
		return vsmTypeValue;
	}

	public void setVsmTypeValue(int vsmTypeValue) {
		this.vsmTypeValue = vsmTypeValue;
	}

	public int getEndInMinutes() {
		return endInMinutes;
	}

	public void setEndInMinutes(int endInMinutes) {
		this.endInMinutes = endInMinutes;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public String getSendToHost() {
		return sendToHostAddress != null ? sendToHostAddress.getHostAddress() : null;
	}

	public void setSendToHost(String sendToHost) throws DPCSubscriptionException {
		try {
			sendToHostAddress = InetAddress.getByName( sendToHost );
		} catch (UnknownHostException ex) {
			throw new DPCSubscriptionException(String.format("Couldn't get host '%s' by name", sendToHost), ex);
		}
	}
	
	public int getRequestID() {
		return requestID;
	}
	
	public void setRequestID() {
		requestID = requestCount.incrementAndGet();
	}
	
	public void setRequestID(int requestID) {
		this.requestID = requestID;
	}
	
	public boolean isSecureEnabled() {
		return secureEnabled;
	}

	public void setSecureEnabled(boolean secureEnabled) throws DecoderException, CertificateException, IOException, CryptoException {
		this.secureEnabled = secureEnabled;
		if (secureEnabled) {
			cryptoProvider = new CryptoProvider();
			loadCertificates();
		}
	}

	public int getPsid() {
		return psid;
	}

	public void setPsid(int psid) {
		this.psid = psid;
	}

	public int request() throws DPCSubscriptionException {
		establishTrust();
		submitSubscriptionRequest();
		return subID;
	}
	
	private void submitSubscriptionRequest() throws DPCSubscriptionException {
		if ( submitSubscriptionRequest(attempts, timeout) == false ) {
			throw new DPCSubscriptionException(String.format("Couldn't submit DataSubscriptionRequest after %d attempts with %d ms timeout", attempts, timeout));
		}
	}
	
	private boolean submitSubscriptionRequest(int attempts, int timeout) throws DPCSubscriptionException  {
		for( int i = 0; i < attempts; i++ ) {
			if ( submitSubscriptionRequest(timeout) )
				return true;
		}
		return false;
	}
	
	private boolean submitSubscriptionRequest(int timeout) throws DPCSubscriptionException {
		DataSubscriptionRequest subscriptionRequest = createDPCSubscriptionRequest();
		System.out.print(subscriptionRequest);
		byte[] requestBytes = null;		
		try {
			requestBytes =  CVSampleMessageBuilder.messageToEncodedBytes(subscriptionRequest);
			if (secureEnabled)
				requestBytes = SecurityHelper.encrypt(requestBytes, certId8, cryptoProvider, psid);
		} catch (EncodeFailedException ex) {
			throw new DPCSubscriptionException("Couldn't encode DataSubscriptionRequest message because encoding failed.", ex);
		} catch (EncodeNotSupportedException ex) {
			throw new DPCSubscriptionException("Couldn't encode DataSubscriptionRequest message because encoding is not supported.", ex);
		}
		
		if ( requestBytes == null )
			return false;

		return submit(requestBytes, timeout);
	}

	public DataSubscriptionRequest createDPCSubscriptionRequest() throws DPCSubscriptionException {
		VsmType type = new VsmType(new byte[] { (byte)(vsmTypeValue) });
		
		return new DataSubscriptionRequest(
				SemiDialogID.dataSubscription, 
				SemiSequenceID.subscriptionReq, 
				groupID,
				TemporaryIDHelper.toTemporaryID(requestID),
				DataSubscriptionRequest.Type.createTypeWithVsmType (type), 
			    endInMinutes(endInMinutes), 
			    geoRegion);
	}
	
	public boolean cancel( int subscriptionID ) throws DPCSubscriptionException {
		this.subID = subscriptionID;
		establishTrust();
		submitSubscriptionCancel();
		subID = -1;
		setRequestID();
		return true;
	}

	boolean cancel( int requestID, int subscriptionID ) throws DPCSubscriptionException {
		setRequestID(requestID);
		return cancel(subscriptionID);
	}
	
	private void submitSubscriptionCancel() throws DPCSubscriptionException {
		if ( submitSubscriptionCancel(attempts, timeout) == false ) {
			throw new DPCSubscriptionException(String.format("Couldn't submit DataSubscriptionRequest after %d attempts with %d ms timeout", attempts, timeout));
		}
	}
	
	private boolean submitSubscriptionCancel(int attempts, int timeout) throws DPCSubscriptionException  {
		for( int i = 0; i < attempts; i++ ) {
			if ( submitSubscriptionCancel(timeout) )
				return true;
		}
		return false;
	}
	
	private boolean submitSubscriptionCancel(int timeout) throws DPCSubscriptionException {
		DataSubscriptionCancel cancelRequest = new DataSubscriptionCancel(
				SemiDialogID.dataSubscription, SemiSequenceID.subscriptionCancel, groupID, TemporaryIDHelper.toTemporaryID(requestID),  TemporaryIDHelper.toTemporaryID(subID));
		System.out.print(cancelRequest);
		byte[] requestBytes = null;		
		try {
			requestBytes =  CVSampleMessageBuilder.messageToEncodedBytes(cancelRequest);
			if (secureEnabled)
				requestBytes = SecurityHelper.encrypt(requestBytes, certId8, cryptoProvider, psid);
		} catch (EncodeFailedException ex) {
			throw new DPCSubscriptionException("Couldn't encode DataSubscriptionCancel message because encoding failed.", ex);
		} catch (EncodeNotSupportedException ex) {
			throw new DPCSubscriptionException("Couldn't encode DataSubscriptionCancel message because encoding is not supported.", ex);
		}
		
		return submit(requestBytes, timeout);
	}
	
	private boolean submit(byte[] requestBytes, int timeout) throws DPCSubscriptionException {		
		if ( requestBytes == null )
			return false;

		DatagramSocket sockSend = null;
		DatagramSocket sockRecv = null;
	    try {
	        DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, sendToHostAddress, sendToPort);
	        
	        byte[] responseBytes = new byte[MAX_PACKET_SIZE];
	        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
 
	        if ( verbose )
	        	System.out.printf("Sending UDP packet to host %s port %d\n", requestPacket.getAddress().getCanonicalHostName(), requestPacket.getPort());
	        sockSend = new DatagramSocket(sendToPort);
	        sockRecv = sendToPort != replyToPort  ? new DatagramSocket(replyToPort) : sockSend;
	        sockRecv.setSoTimeout(timeout);
	        if ( verbose )
	        	System.out.printf("Receiving UDP reply on port %d\n", replyToPort);
	        sockSend.send(requestPacket);
	        sockRecv.receive(responsePacket);
            if  (verbose )
            	System.out.printf("Got response from host %s port %d\n", responsePacket.getAddress().getCanonicalHostName(), responsePacket.getPort());
            return isValidDPCSubscriptionResponse(responsePacket, true);
	    } catch (SocketTimeoutException ex) {
            System.out.println("Socket time out reached. Reason: " + ex);
	    } catch (SocketException ex) {
	        System.out.println("Socket closed. Reason: " + ex);
	    } catch (IOException ex) {
	    	System.out.println("IO Exception. Reason: " + ex);
	    } finally {
	    	if (  sendToPort != replyToPort )
	    		dispose(sockRecv);
	    	dispose(sockSend);
	    }
		return false;
	}
	
	private void dispose(DatagramSocket sock) {
		if ( sock != null ) {
			if ( !sock.isClosed() )
				sock.close();
			sock = null;
		}
	}
	
	private boolean isValidDPCSubscriptionResponse(DatagramPacket resonsePacket, boolean isSubscriptionRequest) throws DPCSubscriptionException {
		if ( resonsePacket == null )
			return false;
		
		final byte[] data = resonsePacket.getData();
		if ( data == null )
			return false;
		
		final int length = resonsePacket.getLength();	
		if( length <= 0 )
			return false;
		
		final String header = isSubscriptionRequest ? "Subscription Request" : "Subscription Cancel";
		
		byte[] responseBytes = Arrays.copyOfRange(data, resonsePacket.getOffset(), length);
		if (secureEnabled) {
			responseBytes = SecurityHelper.decrypt(responseBytes, cryptoProvider);
		}
		
		try {
			
			AbstractData pdu = J2735Util.decode(coder, responseBytes);
			if( pdu == null || !(pdu instanceof DataSubscriptionResponse )) {
				System.out.printf("Unexpected response message of type '%s'\n", pdu != null ? pdu.getClass().getName() : "unknown");
				return false;
			}
			DataSubscriptionResponse response = (DataSubscriptionResponse)pdu;
			System.out.print(response);

			int responseRequestID = TemporaryIDHelper.fromTemporaryID(response.getRequestID());
			if ( requestID != responseRequestID ) {
				System.out.printf("Unexpected %s Response requestID. Expected ID %d. Actual ID: %d\n", header, requestID, responseRequestID);
				return false;
			}
	
			int responseSubID = TemporaryIDHelper.fromTemporaryID(response.getSubID());
			if ( isSubscriptionRequest ) {
				subID = responseSubID;
			} else {
				if ( subID != responseSubID ) {
					System.out.printf("Unexpected %s response subID. Expected ID %d. Actual ID: %d\n", subID, header, responseSubID);
					return false;
				}
			}
			
			if ( response.hasErr() ) {
				throw DPCSubscriptionException.createInstance( header, response.getErr() );
			}

			return true;
		} catch (DecodeFailedException ex) {
			System.out.println("Couldn't decode J2735 ASN.1 BER message because decoding failed");
		} catch (DecodeNotSupportedException ex) {
			System.out.println("Couldn't decode J2735 ASN.1 BER message because decoding is not supported");				
		}
		return false;
	}
	
	private void establishTrust() throws DPCSubscriptionException {
		try {
			TrustEstablishment te = new TrustEstablishment(coder, SemiDialogID.dataSubscription, groupID, requestID, sendToHostAddress, sendToPort, replyToHost, replyToPort);
			if (secureEnabled) {
				te.setSecure(true);
				te.setCryptoProvider(cryptoProvider);
				te.setPsid(psid);
			}
			
			boolean rcode = te.establishTrust(attempts, timeout);
			if (!rcode)
				throw new TrustEstablishmentException("Failed to establish trust");
			
			if (secureEnabled) {
				certId8 = te.getCertId8();
				System.out.println("Trust Establishment returned CertId8: " + Hex.encodeHexString(certId8));
			}

		} catch (UnknownHostException ex ) {
			throw new DPCSubscriptionException("", ex);
		} catch (TrustEstablishmentException ex ) {
			throw new DPCSubscriptionException(String.format("Couldn't establish trust after %d attempts with %d ms timeout", attempts, timeout), ex);
		}
	}
	
	static private DFullTime endInMinutes(int endTimeIntervalInMin) {
		DDateTime ddt = J2735Util.expireInMin(endTimeIntervalInMin);
		return new DFullTime(ddt.getYear(), ddt.getMonth(), ddt.getDay(), ddt.getHour(), ddt.getMinute());
	}
	
	static private Position3D getPosition3D(JSONObject pos) {
		if ( pos == null )
			return null;
		double lat = pos.optDouble("lat", 0);
		double lon = pos.optDouble("lon", 0);
		return CVSampleMessageBuilder.getPosition3D(lat,lon);
	}

	static private GeoRegion getGeoRegion(JSONObject region) {
		if ( region == null )
			return null;
		Position3D nwCnr = null, seCnr = null;
		if ( region.has("nw") )
			nwCnr = getPosition3D(region.getJSONObject("nw"));
		if ( region.has("se") )
			seCnr = getPosition3D(region.getJSONObject("se"));
		return new GeoRegion(nwCnr,seCnr);
	}
	
	private void loadCertificates() throws DecoderException, CertificateException, IOException, CryptoException {
		if ( certs != null ) {
			CertificateManager.clear();
			for ( CertEntry cert : certs ) {
				if ( cert.key == null )
					FileCertificateStore.load(cryptoProvider, cert.name, cert.path);
				else
					FileCertificateStore.load(cryptoProvider, cert.name, cert.path, cert.key);
			}
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
	
		if (args.length < 1) {
			try {
				DPCSubscription subscription = new DPCSubscription();
				subscription.setSendToHost("54.242.96.40");
				subscription.setServiceRegion(42.49589666159403, -83.49163055419922, 42.47678161860101, -83.4550666809082);
				subscription.vsmTypeValue = CVTypeHelper.VsmType.WEATHER.intValue() | CVTypeHelper.VsmType.FUND.intValue();
				subscription.setEndInMinutes(60);	// endTime = now + 60 minutes
			
				int subscriptionID = subscription.request();
				System.out.printf("Subscribed with request id %d and subscription id %d\n", subscription.getRequestID(), subscriptionID);
				// subscribe to topic and process messages here...
				Thread.sleep(10*10000);	 // sleep 10 seconds
				if ( subscription.cancel(subscriptionID) ) {
					System.out.printf("Successfully cancelled subscription with id %d\n", subscriptionID);
				}
			} catch ( DPCSubscriptionException ex ) {
				System.err.printf("Caught DPCSubscriptionException. Reason: %s\n", ex.getMessage());
			}
		} else {
			String configFile = args[0];
			FileInputStream fis = new FileInputStream(configFile);
			String jsonTxt = IOUtils.toString(fis);
			JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonTxt);
			
			try {
				if ( json.has("subscription") )
					json = json.getJSONObject("subscription");
				DPCSubscription subscription = new DPCSubscription(json);
			
				int subscriptionID = subscription.request();
				System.out.printf("Subscribed with subscription id %d\n", subscriptionID);
				// subscribe to topic and process messages here...
				Thread.sleep(10*1000);	 // sleep 10 seconds
				if ( subscription.cancel(subscriptionID) ) {
					System.out.printf("Successfully cancelled subscription with id %d\n", subscriptionID);
				}
				subscription.dispose();
			} catch ( DPCSubscriptionException ex ) {
				System.err.printf("Caught DPCSubscriptionException. Reason: %s\n", ex.getMessage());
			}
		}
	}

	private class CertEntry {
		public final String name;
		public final String path;
		public final String key;
		
		static private final String SECTION_NAME = "cert";
		
		private CertEntry(JSONObject config) {
			JSONObject cert = config.has(SECTION_NAME) ? config.getJSONObject(SECTION_NAME) : new JSONObject();
			name = cert.getString("name");
			path = cert.getString("path");
			key  = cert.optString("key", null);
		}
		
		@Override
		public String toString() {
			return String.format("\t  cert\n\t    name\t%s\n\t    path\t%s\n\t    key\t\t%s", name != null ? name : "", path != null ? path : "", key != null ? key : "");
		}
	}
}
