package gov.usdot.cv.common.database.mongodb.geospatial;

import java.util.ArrayList;

import net.sf.json.JSONArray;

/**
 * Stores a list of coordinate points.
 */
public class Coordinates {
	private Point [] points;
	
	private Coordinates(Point [] points) {
		this.points = points;
	}
	
	/**
	 * @return a json array of the form [[<lon>,<lat>],[<lon>,<lat>],...]
	 */
	public JSONArray toJSONArray() {
		JSONArray arr = new JSONArray();
		for (Point point : this.points) {
			arr.add(point.toJSONArray());
		}
		return arr;
	}
	
	public static class Builder {
		private ArrayList<Point> points = new ArrayList<Point>();
		
		public Builder addPoint(Point point) {
			if (point != null) {
				this.points.add(point);
			}
			return this;
		}
		
		public Coordinates build() {
			return new Coordinates(this.points.toArray(new Point[this.points.size()]));
		}
		
	}
	
}