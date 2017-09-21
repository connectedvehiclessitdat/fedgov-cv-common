package gov.usdot.cv.common.database.mongodb.geospatial;

import net.sf.json.JSONArray;

/**
 * Represent a point in a geospatial coordinate system.
 */
public class Point {
	private double lat;
	private double lon;
	
	private Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	/** 
	 * @return a json array of the form [<lon>,<lat>]
	 */
	public JSONArray toJSONArray() {
		JSONArray arr = new JSONArray();
		arr.add(this.lon);
		arr.add(this.lat);
		return arr;
	}
	
	public static class Builder {
		private Double lat;
		private Double lon;
		
		public Builder setLat(double lat) {
			this.lat = lat;
			return this;
		}
		
		public Builder setLon(double lon) {
			this.lon = lon;
			return this;
		}
		
		public Point build() {
			return new Point(lat, lon);
		}
	}
}