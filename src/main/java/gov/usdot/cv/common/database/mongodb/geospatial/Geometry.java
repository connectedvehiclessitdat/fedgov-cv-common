package gov.usdot.cv.common.database.mongodb.geospatial;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Represents a geojson geometry object. Currently, support geometry of
 * type Point, LineString, and Polygon.
 * 
 * {
 *   "type": "Point", 
 *   "coordinates": [30, 10]
 * }
 * 
 * {
 *   "type": "LineString", 
 *   "coordinates": [
 *       [30, 10], [10, 30], [40, 40]
 *   ]
 * }
 * 
 * {
 *   "type": "Polygon", 
 *   "coordinates": [
 *       [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]
 *   ]
 *  }
 */
public class Geometry {
	public static final String POINT_TYPE = "Point";
	public static final String LINE_STRING_TYPE = "LineString";
	public static final String POLYGON_TYPE = "Polygon";
	
	private static final String TYPE_KEY = "type";
	private static final String COORDINATES_KEY = "coordinates";
	
	private String type;
	private Point point;
	private Coordinates coordinates;
	
	private Geometry(
			String type,
			Point point,
			Coordinates coordinates) {
		this.type = type;
		this.point = point;
		this.coordinates = coordinates;
	}
	
	/**
	 * @return a geojson geometry object.
	 */
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put(TYPE_KEY, this.type);
		
		if (this.type.equals(POINT_TYPE)) {
			obj.put(COORDINATES_KEY, this.point.toJSONArray());
		} else if (this.type.equals(LINE_STRING_TYPE)) {
			obj.put(COORDINATES_KEY, this.coordinates.toJSONArray());
		} else if (this.type.equals(POLYGON_TYPE)) {
			JSONArray arr = new JSONArray();
			arr.add(this.coordinates.toJSONArray());
			obj.put(COORDINATES_KEY, arr);
		} else {
			// Invalid type will not populate the coordinates field
			obj.put(COORDINATES_KEY, new JSONArray());
		}
		
		return obj;
	}
	
	public static class Builder {
		private String type;
		private Point point;
		private Coordinates coordinates;
		
		public Builder setType(String type) {
			this.type = type;
			return this;
		}
		
		public Builder setPoint(Point point) {
			this.point = point;
			return this;
		}
		
		public Builder setCoordinates(Coordinates coordinates) {
			this.coordinates = coordinates;
			return this;
		}
		
		public Geometry build() {
			return new Geometry(this.type, this.point, this.coordinates);
		}
	}
}