package gov.usdot.cv.common.dialog;

import gov.usdot.asn1.generated.j2735.dsrc.DDateTime;
import gov.usdot.asn1.generated.j2735.dsrc.TemporaryID;
import gov.usdot.asn1.generated.j2735.semi.ConnectionPoint;
import gov.usdot.asn1.generated.j2735.semi.GroupID;
import gov.usdot.asn1.generated.j2735.semi.SemiDialogID;
import gov.usdot.asn1.generated.j2735.semi.ServiceRequest;
import gov.usdot.asn1.generated.j2735.semi.ServiceResponse;
import gov.usdot.asn1.j2735.CVSampleMessageBuilder;
import gov.usdot.asn1.j2735.J2735Util;
import gov.usdot.cv.common.asn1.ConnectionPointHelper;
import gov.usdot.cv.common.asn1.TemporaryIDHelper;
import gov.usdot.cv.security.cert.Certificate;
import gov.usdot.cv.security.cert.CertificateManager;
import gov.usdot.cv.security.crypto.CryptoProvider;
import gov.usdot.cv.security.msg.IEEE1609p2Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import com.oss.asn1.AbstractData;
import com.oss.asn1.Coder;
import com.oss.asn1.DecodeFailedException;
import com.oss.asn1.DecodeNotSupportedException;
import com.oss.asn1.EncodeFailedException;
import com.oss.asn1.EncodeNotSupportedException;

public class TrustEstablishment {
	
	private static final Logger log = Logger.getLogger(TrustEstablishment.class);
	
	static private final String DIGEST_ALGORITHM_NAME = "SHA-256"; 
	private static final int MAX_PACKET_SIZE = 2048;
	
	private InetAddress hostAddress = null;
	private final int sendToPort;
	private final int sendFromPort;
	private final SemiDialogID dialogID;
	private final GroupID groupID;
	private final Coder coder;
	private final int requestID;
	private final ConnectionPoint destConnection;

	private int attempts = 3;
	private int timeout = 4000;
	private boolean verbose = false;
	private boolean secure = false;
	private int psid = 0x2fe1;
	private String selfCertificateFriendlyName = "Self";
	private IEEE1609p2Message msg1609p2;
	private CryptoProvider cryptoProvider;
	private byte[] certId8;
	
	public static void establishTrust(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, String host, int sendToPort) throws UnknownHostException, TrustEstablishmentException {
		new TrustEstablishment(coder, dialogID, groupID, requestID, host, sendToPort).establishTrust();
	}
	
	public static void establishTrust(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort) throws TrustEstablishmentException {
		new TrustEstablishment(coder, dialogID, groupID, requestID, hostAddress, sendToPort).establishTrust();
	}
	
	public static void establishTrust(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, String host, int sendToPort, String destHost, int destPort) throws UnknownHostException, TrustEstablishmentException {
		new TrustEstablishment(coder, dialogID, groupID, requestID, InetAddress.getByName(host), sendToPort, InetAddress.getByName( destHost ), destPort, sendToPort).establishTrust();
	}
	
	public static void establishTrust(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort, InetAddress destAddress, int destPort) throws TrustEstablishmentException {
		new TrustEstablishment(coder, dialogID, groupID, requestID, hostAddress, sendToPort, destAddress, destPort, sendToPort).establishTrust();
	}
	
	public static void establishTrust(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort, InetAddress destAddress, int destPort, int sendFromPort) throws TrustEstablishmentException {
		new TrustEstablishment(coder, dialogID, groupID, requestID, hostAddress, sendToPort, destAddress, destPort, sendFromPort).establishTrust();
	}

	public TrustEstablishment(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, String host, int sendToPort) throws UnknownHostException {
		this(coder, dialogID, groupID, requestID, InetAddress.getByName( host ), sendToPort);
	}
	
	public TrustEstablishment(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort) {
		this(coder, dialogID, groupID, requestID, hostAddress, sendToPort, (InetAddress)null, -1, sendToPort);
	}
	
	public TrustEstablishment(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort, String destHost, int destPort) throws UnknownHostException {
		this(coder, dialogID, groupID, requestID, hostAddress, sendToPort, InetAddress.getByName( destHost ), destPort, sendToPort);
	}
	
	public TrustEstablishment(Coder coder, SemiDialogID dialogID, GroupID groupID, int requestID, InetAddress hostAddress, int sendToPort, InetAddress destAddress, int destPort, int sendFromPort) {
		this.coder = coder;
		this.dialogID = dialogID;
		this.groupID = groupID;
		this.requestID = requestID;
		this.hostAddress = hostAddress;
		this.sendToPort = sendToPort;
		this.sendFromPort = sendFromPort;

		boolean haveDestIPAddress = destAddress != null && !destAddress.equals(hostAddress);
		boolean haveDestPort = destPort != -1 && destPort != this.sendToPort;
		
		if ( haveDestIPAddress || haveDestPort ) {
			if ( !haveDestPort )
				destPort = this.sendToPort;
			destConnection = haveDestIPAddress ? 
				ConnectionPointHelper.createConnectionPoint(destAddress, destPort) : 
				ConnectionPointHelper.createConnectionPoint(destPort);
		} else {
			destConnection = null;
		}
	}
	
	public void establishTrust() throws TrustEstablishmentException {
		if ( establishTrust(attempts, timeout) == false ) {
			throw new TrustEstablishmentException(String.format("Couldn't establish trust after %d attempts with %d ms timeout", attempts, timeout));
		}
	}
	
	public boolean establishTrust(int attempts, int timeout) throws TrustEstablishmentException  {
		if ( secure && msg1609p2 == null ) {
			if ( cryptoProvider == null )
				cryptoProvider = new CryptoProvider();
			msg1609p2 = new IEEE1609p2Message(cryptoProvider);
			msg1609p2.setPSID(psid);
		}
		for( int i = 0; i < attempts; i++ ) {
			if ( establishTrust(timeout) )
				return true;
		}
		return false;
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
	
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getSelfCertificateFriendlyName() {
		return selfCertificateFriendlyName;
	}

	public void setSelfCertificateFriendlyName(String selfCertificateFriendlyName) {
		this.selfCertificateFriendlyName = selfCertificateFriendlyName;
	}
	
	public int getPsid() {
		return psid;
	}

	public void setPsid(int psid) {
		this.psid = psid;
	}
	
	public CryptoProvider getCryptoProvider() {
		return cryptoProvider;
	}

	public void setCryptoProvider(CryptoProvider cryptoProvider) {
		this.cryptoProvider = cryptoProvider;
	}

	public byte[] getCertId8() {
		return certId8;
	}

	private boolean establishTrust(int timeout) throws TrustEstablishmentException {
		TemporaryID reqID = TemporaryIDHelper.toTemporaryID(requestID);
		ServiceRequest request = destConnection != null ?
				CVSampleMessageBuilder.buildServiceRequest(reqID, dialogID, destConnection, groupID) :
				CVSampleMessageBuilder.buildServiceRequest(reqID, dialogID);
		if ( verbose )
			log.info("ServiceRequest\n" + request);
		
		byte[] requestBytes = null;
		byte[] requestHash = null;		
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM_NAME);
			requestBytes =  CVSampleMessageBuilder.messageToEncodedBytes(request);
			requestHash =  messageDigest.digest(requestBytes);
		} catch (EncodeFailedException ex) {
			throw new TrustEstablishmentException("Couldn't encode ServiceRequest message because encoding failed.", ex);
		} catch (EncodeNotSupportedException ex) {
			throw new TrustEstablishmentException("Couldn't encode ServiceRequest message because encoding is not supported.", ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new TrustEstablishmentException(String.format("Couldn't instantiate digest algorithm %s.", DIGEST_ALGORITHM_NAME), ex );
		}
		
		if ( requestBytes == null )
			return false;
		
		if ( secure ) {
			try {
				assert(msg1609p2 != null);
				requestBytes = msg1609p2.sign(requestBytes);
			} catch (Exception ex) {
				throw new TrustEstablishmentException("Couldn't create signed 1609.2 message. Reason: " + ex.getMessage(), ex);
			}
		}

		DatagramSocket sock = null;
		DatagramSocket sockReceive = null;
	    try {
	        DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length, hostAddress, sendToPort);
	        
	        byte[] responseBytes = new byte[MAX_PACKET_SIZE];
	        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
	        
	        if ( verbose )
	        	log.info(String.format("Sending %sUDP packet to host %s port %d\n", 
	        			secure ? "signed " : "", requestPacket.getAddress().getCanonicalHostName(), requestPacket.getPort()));
	        sock = new DatagramSocket(sendFromPort);
	        sockReceive = 
	        		destConnection != null ? new DatagramSocket((int)destConnection.getPort().longValue()) : 
	        		sendFromPort != sendToPort ? new DatagramSocket(sendToPort) : sock;
	        sockReceive.setSoTimeout(timeout);
	        sock.send(requestPacket);
	        sockReceive.receive(responsePacket);
            if  (verbose )
            	log.info(String.format("Got response from host %s port %d\n", responsePacket.getAddress().getCanonicalHostName(), responsePacket.getPort()));
            return isValidServiceResonse(responsePacket, requestHash);
	    } catch (SocketTimeoutException ex) {
            log.warn(String.format("Socket time out reached while receiving reply. Reason: %s", ex.getMessage()), ex);
	    } catch (SocketException ex) {
	    	log.warn(String.format("Socket Exception. Reason: %s", ex.getMessage()), ex);
	    } catch (IOException ex) {
	    	log.warn(String.format("IO Exception. Reason: %s", ex.getMessage()), ex);
	    } finally {
			if ( sock != null ) {
				if ( !sock.isClosed() )
					sock.close();
				sock = null;
			}
			if ( sockReceive != null ) {
				if ( !sockReceive.isClosed() )
					sockReceive.close();
				sockReceive = null;
			}
	    }
		return false;
	}
	
	private boolean isValidServiceResonse(DatagramPacket resonsePacket, byte[] requestHash) throws TrustEstablishmentException {
		if ( resonsePacket == null )
			return false;
		
		final byte[] data = resonsePacket.getData();
		if ( data == null )
			return false;
		
		final int length = resonsePacket.getLength();	
		if( length <= 0 )
			return false;
		
		byte[] resonseBytes = Arrays.copyOfRange(data, resonsePacket.getOffset(), length);
		
		if ( secure ) {
			try {
				IEEE1609p2Message response = IEEE1609p2Message.parse(resonseBytes, cryptoProvider);
				assert(response != null);
				Certificate cert = response.getCertificate();
				assert(cert != null);
				certId8 = cert.getCertID8();
				resonseBytes = response.getPayload();
				assert(CertificateManager.get(certId8) != null);
			} catch (Exception ex) {
				throw new TrustEstablishmentException("Couldn't parse secure ServiceResponse. Reason: " + ex.getMessage(), ex);
			}
		}

		try {		
			AbstractData pdu = J2735Util.decode(coder, resonseBytes);
			if( pdu == null || !(pdu instanceof ServiceResponse )) {
				log.warn(String.format("Unexpected response message of type '%s'\n", pdu != null ? pdu.getClass().getName() : "unknown"));
				return false;
			}
			ServiceResponse response = (ServiceResponse)pdu;
			if ( verbose )
				log.info("ServiceResponse\n" + response);
			long expectedDialogID = dialogID.longValue();
			if ( expectedDialogID != dialogID.longValue() ) {
				log.warn(String.format("Unexpected response dialog ID. Expected ID %d. Actual ID: %d\n",	expectedDialogID, response.getDialogID().longValue()));
				return false;
			}
			int responseRequestID = TemporaryIDHelper.fromTemporaryID(response.getRequestID());
			if ( requestID != responseRequestID ) {
				log.warn(String.format("Unexpected ServiceResponse requestID. Expected ID %d. Actual ID: %d\n", requestID, responseRequestID));
				return false;
			}
			DDateTime expiration = response.getExpiration();
			if ( J2735Util.isExpired(expiration) ) {
				log.warn(String.format("ServiceResponse message has expired. Expiration time: %s. Current time: %s.\n",
						J2735Util.formatCalendar(J2735Util.DDateTimeToCalendar(expiration)), J2735Util.formatCalendar(GregorianCalendar.getInstance())));
				return false;
			}
			byte[] hash = response.getHash().byteArrayValue();
			if ( !Arrays.equals(requestHash, hash) ) {
				log.warn("ServiceResponse message hash validation failed");
				return false;
			}
			if ( response.hasServiceRegion() ) {
				// add code to validate region
			}
			return true;
		} catch (DecodeFailedException ex) {
			log.error("Couldn't decode J2735 ASN.1 BER message because decoding failed", ex);
		} catch (DecodeNotSupportedException ex) {
			log.error("Couldn't decode J2735 ASN.1 BER message because decoding is not supported", ex);				
		}
		return false;
	}
}
