package gov.usdot.cv.common.model;


public class BoundingBox {
	public static final double MIN_LAT = -90.0;
	public static final double MAX_LAT = 90.0;
	public static final double MIN_LON = -180.0;
	public static final double MAX_LON = 180.0;
	
	private Double nwLat;
	private Double nwLon;
	private Double seLat;
	private Double seLon;
	private String validationError;
	
	private BoundingBox(
			Double nwLat, 
			Double nwLon, 
			Double seLat, 
			Double seLon) {
		this.nwLat = nwLat;
		this.nwLon = nwLon;
		this.seLat = seLat;
		this.seLon = seLon;
	}
	
	public Double getNWLat() { return this.nwLat; }
	public Double getNWLon() { return this.nwLon; }
	public Double getSELat() { return this.seLat; }
	public Double getSELon() { return this.seLon; }
	
	public boolean contains(double lat, double lon) {
		return (lat <= this.nwLat && lat >= this.seLat && 
				lon >= this.nwLon && lon <= this.seLon);
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("nw-lat=").append(this.nwLat).append(',');
		sb.append("nw-lon=").append(this.nwLon).append(',');
		sb.append("se-lat=").append(this.seLat).append(',');
		sb.append("se-lon=").append(this.seLon);
		return sb.toString();
	}
	
	public static class Builder {
		private Double nwLat;
		private Double nwLon;
		private Double seLat;
		private Double seLon;
		
		public Builder setNWLat(Double lat) { 
			this.nwLat = lat;
			return this; 
		}
		
		public Builder setNWLon(Double lon) {
			this.nwLon = lon;
			return this; 
		}
		
		public Builder setSELat(Double lat) { 
			this.seLat = lat;
			return this; 
		}
		
		public Builder setSELon(Double lon) { 
			this.seLon = lon;
			return this; 
		}
		
		public BoundingBox build() {
			if (nwLat == null || nwLon == null || 
				seLat == null || seLon == null) {
				return null;
			}
			
			return new BoundingBox(
					this.nwLat, 
					this.nwLon, 
					this.seLat, 
					this.seLon);
		}
	}
	
	public boolean isValid() {
		if (this.getNWLat() == null) {
			this.validationError = "NW latitude is not set.";
			return false;
		}
		
		if (this.getNWLon() == null) {
			this.validationError = "NW longitude is not set.";
			return false;
		}
		
		if (this.getSELat() == null) {
			this.validationError = "SE latitude is not set.";
			return false;
		}
		
		if (this.getSELon() == null) {
			this.validationError = "SE longitude is not set.";
			return false;
		}
		
		if (this.getNWLat() > BoundingBox.MAX_LAT || this.getNWLat() < BoundingBox.MIN_LAT) {
			this.validationError = "Invalid NW latitude value.";
			return false;
		}
		
		if (this.getNWLon()> BoundingBox.MAX_LON || this.getNWLon() < BoundingBox.MIN_LON) {
			this.validationError = "Invalid NW longitude value.";
			return false;
		}
		
		if (this.getSELat() > BoundingBox.MAX_LAT || this.getSELat() < BoundingBox.MIN_LAT) {
			this.validationError = "Invalid SE latitude value.";
			return false;
		}
		
		if (this.getSELon() > BoundingBox.MAX_LON || this.getSELon() < BoundingBox.MIN_LON) {
			this.validationError = "Invalid SE longitude value.";
			return false;
		}
		
		if (this.getNWLat() < this.getSELat() || this.getNWLon() > this.getSELon()) {
			this.validationError = "NW and SE positions doesn't form a bounding box.";
			return false;
		}
		return true;
	}
	
	public String getValidationError() {
		return this.validationError;
	}
}