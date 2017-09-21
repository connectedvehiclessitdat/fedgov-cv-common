package gov.usdot.cv.common.asn1.model;

import net.sf.json.JSONObject;

public class IntersectionLane {

	private GPSCoordinate coordinate;
	private String id;
	private String lightColor;
	private String signalType;
	
	public GPSCoordinate getCoordinate() {
		return coordinate;
	}
	public void setCoordinate(GPSCoordinate coordinate) {
		this.coordinate = coordinate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLightColor() {
		return lightColor;
	}
	public void setLightColor(String lightColor) {
		this.lightColor = lightColor;
	}
	public String getSignalType() {
		return signalType;
	}
	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.element("lane", id);
		json.element("type", signalType);
		json.element("lat", coordinate.getLat());
		json.element("long", coordinate.getLon());
		json.element("lightColor", lightColor);
		return json;
	}
}
