package gov.usdot.cv.common.asn1.model;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Intersection {
	
	private GPSCoordinate refPoint;
	private ArrayList<IntersectionLane> lanes = new ArrayList<IntersectionLane>();
	
	public Intersection() {
		super();
	}

	public GPSCoordinate getRefPoint() {
		return refPoint;
	}

	public void setRefPoint(GPSCoordinate refPoint) {
		this.refPoint = refPoint;
	}
	
	public ArrayList<IntersectionLane> getLanes() {
		return lanes;
	}

	public void addLane(IntersectionLane lane) {
		lanes.add(lane);
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.element("intersection", 1);
		json.element("centerPoint", refPoint.toJSON());
		
		JSONArray roads = new JSONArray();
		for (IntersectionLane lane: lanes) {
			roads.element(lane.toJSON());
		}
		json.element("roads", roads);
		
		return json;
	}
}
