package gov.usdot.cv.common.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Filter {
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String UTC_TIMEZONE = "UTC";
	
	private Integer subscriberId;
	private Calendar endTime;
	private String type;
	private Integer typeValue;
	private Integer requestId;
	private BoundingBox boundingBox;
	
	private Filter(
			int subscriberId,
			Calendar endTime, 
			String type,
			int typeValue,
			int requestId,
			BoundingBox boundingBox) {
		this.subscriberId = subscriberId;
		this.endTime = endTime;
		this.type = type;
		this.typeValue = typeValue;
		this.requestId = requestId;
		this.boundingBox = boundingBox;
	}
	
	public Integer getSubscriberId() 		{ return this.subscriberId.intValue(); }
	public Calendar getEndTime() 			{ return this.endTime; }
	public String getType() 				{ return this.type; }
	public Integer getTypeValue() 			{ return this.typeValue; }
	public Integer getRequestId()			{ return this.requestId; }
	public BoundingBox getBoundingBox() 	{ return this.boundingBox; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("subscriber-id=").append(this.subscriberId).append(',');
		sb.append("end-time=").append(this.endTime.getTime()).append(',');
		sb.append("type=").append(this.type).append(',');
		sb.append("type-value=").append(this.typeValue);
		if (this.boundingBox != null) {
			sb.append(',').append(this.boundingBox.toString());
		}
		return sb.toString();
	}
	
	public static class Builder {
		private Integer subscriberId;
		private Calendar endTime;
		private String type;
		private Integer typeValue;
		private Integer requestId;
		private BoundingBox boundingBox;
		
		public Builder setSubscriberId(int subscriberId) {
			this.subscriberId = subscriberId;
			return this;
		}
		
		public Builder setEndTime(Calendar endTime) {
			this.endTime = endTime;
			return this;
		}
		
		public Builder setEndTime(String end) throws ParseException {
			DateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
			this.endTime = Calendar.getInstance(TimeZone.getTimeZone(UTC_TIMEZONE));
			this.endTime.setTimeInMillis(formatter.parse(end).getTime());
			return this;
		}
		
		public Builder setType(String type) {
			this.type = type;
			return this;
		}
		
		public Builder setTypeValue(int typeValue) {
			this.typeValue = typeValue;
			return this;
		}
		
		public Builder setRequestId(int requestId) {
			this.requestId = requestId;
			return this;
		}
		
		public Builder setBoundingBox(BoundingBox boundingBox) {
			this.boundingBox = boundingBox;
			return this;
		}
		
		public Filter build() {
			return new Filter(
				this.subscriberId, 
				this.endTime, 
				this.type,
				this.typeValue,
				this.requestId,
				this.boundingBox);
		}
	}
}