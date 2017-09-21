package gov.usdot.cv.common.asn1.model;

import net.sf.json.JSONObject;

public class SignalLight {

	private int laneNumber;
	private String lightColor;
	private String arrowType;
	
	public int getLaneNumber() {
		return laneNumber;
	}
	public void setLaneNumber(int laneNumber) {
		this.laneNumber = laneNumber;
	}
	public String getLightColor() {
		return lightColor;
	}
	public void setLightColor(String lightColor) {
		this.lightColor = lightColor;
	}
	public String getArrowType() {
		return arrowType;
	}
	public void setArrowType(String arrowType) {
		this.arrowType = arrowType;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.element("laneNumber", laneNumber);
		json.element("lightColor", lightColor);
		json.element("arrowType", arrowType);
		return json;
	}
}
