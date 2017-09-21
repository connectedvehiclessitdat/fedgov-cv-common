package gov.usdot.cv.common.asn1.model;

import net.sf.json.JSONObject;

public class GPSCoordinate {

	private Double lat;
	private Double lon;

	public GPSCoordinate(Double lat, Double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.element("lat", lat);
		json.element("long", lon);
		return json;
	}

	public GPSCoordinate fromOffsets2(double xOffsetInMeters, double yOffsetInMeters) {
		// http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
		
		// Earth’s radius, sphere
		int radius = 6378137;

		// Coordinate offsets in radians
		double dLat = yOffsetInMeters/radius;
		double dLon = xOffsetInMeters/(radius*Math.cos(Math.PI*lat/180));

		// OffsetPosition, decimal degrees
		double latO = lat + dLat * 180/Math.PI;
		double lonO = lon + dLon * 180/Math.PI; 
		
		// New offset Coordinate
		return new GPSCoordinate(latO, lonO);
	}
	
	public GPSCoordinate fromOffsets(double xOffsetInMeters, double yOffsetInMeters) {
		// http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
		
		int magic = 111111;
		double latO = lat + (yOffsetInMeters/magic);
		double lonO = lon + (xOffsetInMeters/magic);
		
		// New offset Coordinate
		return new GPSCoordinate(latO, lonO);
	}

	@Override
	public String toString() {
		return "GPSCoordinate [lat=" + lat + ", lon=" + lon + "]";
	}
	
}
