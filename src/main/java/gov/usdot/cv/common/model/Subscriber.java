package gov.usdot.cv.common.model;

public class Subscriber {
	private Integer subscriberId;
	private byte [] certificate;
	private String destHost;
	private Integer destPort;
	private Filter filter;
	
	private Subscriber(
			Integer subscriberId, 
			byte [] certificate,
			String destHost,
			Integer destPort,
			Filter filter) {
		this.subscriberId = subscriberId;
		this.certificate = certificate;
		this.destHost = destHost;
		this.destPort = destPort;
		this.filter = filter;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || ! (other instanceof Subscriber)) {
			return false;
		}
		return (this.subscriberId == ((Subscriber) other).subscriberId);
	}
	
	@Override
	public int hashCode() {
		return 31 * 17 + subscriberId;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("subscriberId=").append(this.subscriberId).append(',');
		sb.append("certificate=").append((this.certificate != null)).append(',');
		sb.append("destHost=").append(this.destHost).append(',');
		sb.append("destPort=").append(this.destPort);
		if (this.filter != null) {
			sb.append(',').append(this.filter.toString());
		}
		return sb.toString();
	}
	
	public Integer getSubscriberId() 	{ return this.subscriberId; }
	public byte [] getCertificate() 	{ return this.certificate; }
	public String getDestHost() 		{ return this.destHost; }
	public Integer getDestPort() 		{ return this.destPort; }
	public Filter getFilter()			{ return this.filter; }
	
	public static class Builder {
		private Integer subscriberId;
		private byte [] certificate;
		private String destHost;
		private Integer destPort;
		private Filter filter;
		
		public Builder setSubscriberId(int subscriberId) { 
			this.subscriberId = subscriberId;
			return this; 
		}
		
		public Builder setCertificate(byte [] certificate) {
			this.certificate = certificate;
			return this; 
		}
		
		public Builder setDestHost(String destHost) {
			this.destHost = destHost;
			return this;
		}
		
		public Builder setDestPort(int destPort) {
			this.destPort = destPort;
			return this;
		}
		
		public Builder setFilter(Filter filter) {
			this.filter = filter;
			return this;
		}
		
		public Subscriber build() {
			return new Subscriber(
					this.subscriberId, 
					this.certificate,
					this.destHost,
					this.destPort,
					this.filter);
		}
	}
}