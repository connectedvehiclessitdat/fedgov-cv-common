package gov.usdot.cv.common.database.mongodb.criteria;

import java.util.Date;

public class DateRange {
	private String fieldName;
	private Long startTime;
	private Long endTime;
	
	private DateRange(
		String fieldName, 
		Long startTime, 
		Long endTime) {
		this.fieldName = fieldName;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public String getFieldName() { 
		return this.fieldName; 
	}
	
	public boolean hasStartTime() {
		return (this.startTime != null);
	}
	
	public Date getStartTime() { 
		if (this.startTime == null) return null;
		return new Date(this.startTime); 
	}
	
	public boolean hasEndTime() {
		return (this.endTime != null);
	}
	
	public Date getEndTime() {
		if (this.endTime == null) return null;
		return new Date(this.endTime); 
	}
	
	public static class Builder {
		private String fieldName;
		private Long startTime;
		private Long endTime;
		
		public Builder setFieldName(String fieldName) {
			this.fieldName = fieldName;
			return this;
		}
		
		public Builder setStartTime(Long startTime) {
			this.startTime = startTime;
			return this;
		}
		
		public Builder setEndTime(Long endTime) {
			this.endTime = endTime;
			return this;
		}
		
		public DateRange build() {
			if (this.fieldName == null || (this.startTime == null && this.endTime == null)) {
				return null;
			}
			
			return new DateRange(
				this.fieldName, 
				this.startTime, 
				this.endTime);
		}
	}
	
}