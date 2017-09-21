package gov.usdot.cv.common.dialog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class DataBundleUtil {
	
	private static final Logger log = Logger.getLogger(DataBundleUtil.class);
	
	private static final int UUID_LENGTH = 36;
	
	private DataBundleUtil() {
		// Uses the static methods
	}
	
	/**
	 * Prefix the byte array with a uuid as the receipt id.
	 */
	public static byte [] prependReceiptId(byte [] payload) {
		return prependReceiptId(UUID.randomUUID(), payload);
	}
	
	/**
	 * Takes the given uuid and prefix the byte array with it as the receipt id.
	 */
	public static byte [] prependReceiptId(UUID uuid, byte [] payload) {
		return prependReceiptId(uuid.toString(), payload);
	}
	
	/**
	 * Takes the given uuid as string and prefix the byte array with it as the receipt id.
	 */
	public static byte [] prependReceiptId(String uuid, byte [] payload) {
		byte [] uuidByteArr = uuid.getBytes();
		byte [] payloadWithUUID = new byte[uuidByteArr.length + payload.length];
		System.arraycopy(uuidByteArr, 0, payloadWithUUID, 0, uuidByteArr.length);
		System.arraycopy(payload, 0, payloadWithUUID, uuidByteArr.length, payload.length);
		return payloadWithUUID;
	}
	
	/**
	 * Converts data bundle to a format suitable for putting on the JMS queue.
	 */
	public static String encode(DataBundle dataBundle) {
		byte[] receiptIdBytes = dataBundle.getReceiptId().getBytes();
		String destHost = dataBundle.getDestHost();
		byte[] destHostBytes = destHost != null ? destHost.getBytes() : null;
		int destPort = dataBundle.getDestPort();
		boolean fromForwarder = dataBundle.fromForwarder();
		byte[] certificate = dataBundle.getCertificate();
		byte[] payload = dataBundle.getPayload();
		return encode(receiptIdBytes, destHostBytes, destPort, fromForwarder, certificate, payload);
	}
	
	/**
	 * Converts data bundle parts to a format suitable for putting on the JMS queue.
	 */
	public static String encode(
			byte[] receiptIdBytes,
			byte[] destHostBytes,
			int destPort,
			boolean fromForwarder,
			byte[] certificate,
			byte[] payload) {
		int length = 4 + 1 +
				encodedByteArraySize(receiptIdBytes) +
				encodedByteArraySize(destHostBytes) +
				encodedByteArraySize(certificate) +
				encodedByteArraySize(payload);
		ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
		encodeByteArray(buffer, receiptIdBytes);
		encodeByteArray(buffer, destHostBytes);
		buffer.putInt(destPort);
		buffer.put((byte)(fromForwarder ? 1 : 0));
		encodeByteArray(buffer, certificate);
		encodeByteArray(buffer, payload);
		return new String( Base64.encodeBase64(buffer.array()) );
	}
	
	/**
	 * Converts encoded data bundle to a DataBundle instance.
	 */
	public static DataBundle decode(String encodedDataBundle) {
		byte[] bundle = Base64.decodeBase64(encodedDataBundle);
		if ( bundle == null )
			return null;
		try {
			ByteBuffer buffer = ByteBuffer.wrap(bundle);
			String receiptId = new String( decodeByteArray(buffer, "receiptId") );
			byte[] destHostBytes = decodeByteArray(buffer, "destHost" );
			String destHost  = destHostBytes != null ? new String( destHostBytes ) : null;
			int destPort = decodeInt(buffer, "destPort");
			boolean fromForwarder = decodeBoolean(buffer, "fromForwarder");
			byte[] certificate = decodeByteArray(buffer, "certificate" );
			byte[] payload = decodeByteArray(buffer, "payload" );
			
			DataBundle.Builder builder = new DataBundle.Builder();
			builder.setReceiptId(receiptId).setDestHost(destHost).setDestPort(destPort)
				.setFromForwarder(fromForwarder).setPayload(payload).setCertificate(certificate);
			
			return builder.build();
		} catch (IllegalArgumentException ex) {
			log.warn(ex.getMessage());
		}
		return null;
	}
	
	private static void encodeByteArray(ByteBuffer buffer, byte[] bytes) {
		if ( bytes != null ) {
			buffer.putInt(bytes.length);
			buffer.put(bytes);
		} else {
			buffer.putInt(0);
		}
	}
	
	private static int encodedByteArraySize(byte[] bytes) {
		return 4 + (bytes != null ? bytes.length : 0);
	}
	
	private static byte[] decodeByteArray(ByteBuffer buffer, String loggerHint)  throws IllegalArgumentException {
		assert(buffer != null);
		if ( buffer.remaining() < 4 ) 
			throw new IllegalArgumentException("Coulnd't decode DataBundle because the buffer is too short to get length of " + loggerHint);
		int length = buffer.getInt();
		if ( length == 0 )
			return null;
		if ( buffer.remaining() < length ) 
			throw new IllegalArgumentException(String.format("Coulnd't decode DataBundle because the buffer is too short to get %s of length %s", loggerHint, length));
		byte[] bytes = new byte[length];
		buffer.get(bytes, 0, length);
		return bytes;
	}
	
	private static int decodeInt(ByteBuffer buffer, String loggerHint) throws IllegalArgumentException {
		assert(buffer != null);
		if ( buffer.remaining() < 4 ) 
			throw new IllegalArgumentException("Coulnd't decode DataBundle because the buffer is too short to get " + loggerHint);
		return buffer.getInt();
	}
	
	private static boolean decodeBoolean(ByteBuffer buffer, String loggerHint) throws IllegalArgumentException {
		assert(buffer != null);
		if ( buffer.remaining() < 1 ) 
			throw new IllegalArgumentException("Coulnd't decode DataBundle because the buffer is too short to get " + loggerHint);
		return buffer.get() == 1 ? true : false;
	}
	
	/**
	 * Base64 decodes the payload, unwraps it, and return a data bundle object.
	 */
	public static DataBundle decodeBase64AndUnwrap(String payloadWithUUID) {
		return unwrap(Base64.decodeBase64(payloadWithUUID));
	}
	
	/**
	 * Unwraps a base64 decode byte array into a data bundle object.
	 */
	public static DataBundle unwrap(byte [] payloadWithUUID) {
		byte [] uuid = new byte[UUID_LENGTH];
		byte [] payload = new byte[payloadWithUUID.length - UUID_LENGTH];
		
		System.arraycopy(payloadWithUUID, 0, uuid, 0, uuid.length);
		System.arraycopy(payloadWithUUID, UUID_LENGTH, payload, 0, payload.length);
		
		DataBundle.Builder builder = new DataBundle.Builder();
		builder.setReceiptId(new String(uuid)).setPayload(payload);
		
		return builder.build();
	}
	
}