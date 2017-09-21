package gov.usdot.cv.common.dialog;

import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

/**
 * The internal representation of the Connected Vehicles message.
 */
public class DataBundle {
	/** The receipt id for this message bundle. */
	private String uuid;
	
	/** Response source hostname. */
	private String destHost;
	
	/** Response source port. */
	private int destPort;
	
	/** If the request came from the forwarder. */
	private boolean fromForwarder;
	
	/** ASN.1 BER encoded bundle. */
	private byte [] payload;
	
	/** Client's certificate */
	private byte [] certificate;
	
	private DataBundle(
			String uuid, 
			String destHost, 
			int destPort, 
			boolean fromForwarder, 
			byte [] payload,
			byte [] certificate) {
		this.uuid = uuid;
		this.destHost = destHost;
		this.destPort = destPort;
		this.fromForwarder = fromForwarder;
		this.payload = payload;
		this.certificate = certificate;
	}
	
	public String getReceiptId() {
		return this.uuid;
	}
	
	public String getDestHost() {
		return this.destHost;
	}
	
	public int getDestPort() {
		return this.destPort;
	}
	
	public boolean fromForwarder() {
		return this.fromForwarder;
	}
	
	public byte [] getPayload() {
		return this.payload;
	}
	
	public byte [] getCertificate() {
		return certificate;
	}
	
	public String encode() {
		return DataBundleUtil.encode(this);
	}
	
	public String encodePayload() {
		String encoded = Base64.encodeBase64String(this.payload);
		encoded = processInternal(encoded);
		return encoded;
	}
	
	private String processInternal(String value) {
		value = stripNewline(value);
		return value;
	}
	
	private String stripNewline(String value) {
		if (value == null) return null;
		return value.replace("\n", "").replace("\r", "");
	}
	
	public static class Builder {
		private String uuid;
		private String destHost;
		private Integer destPort = -1;
		private boolean fromForwarder = false;
		private byte [] payload;
		private byte [] certificate;
		
		public Builder setReceiptId(String uuid) {
			this.uuid = uuid;
			return this;
		}
		
		public Builder setDestHost(String destHost) {
			this.destHost = destHost;
			return this;
		}
		
		public Builder setDestPort(Integer destPort) {
			this.destPort = destPort;
			return this;
		}
		
		public Builder setFromForwarder(boolean fromForwarder) {
			this.fromForwarder = fromForwarder;
			return this;
		}
		
		public Builder setPayload(byte [] payload) {
			this.payload = payload;
			return this;
		}
		
		public Builder setCertificate(byte [] certificate) {
			this.certificate = certificate;
			return this;
		}
		
		public DataBundle build() {
			// No receipt id is present, create one.
			if (this.uuid == null) {
				this.uuid = UUID.randomUUID().toString();
			}
			
			if (this.destHost == null || this.destPort == null) {
				throw new NullPointerException("Destination host/port is null.");
			}
			
			if (this.payload == null || this.payload.length == 0) {
				throw new NullPointerException("Payload is null or have length 0.");
			}
			
			return new DataBundle(
					this.uuid, 
					this.destHost, 
					this.destPort, 
					this.fromForwarder, 
					this.payload,
					this.certificate);
		}
	}
}