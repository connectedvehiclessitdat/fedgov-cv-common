package gov.usdot.cv.common.dialog;

import net.sf.json.JSONObject;

public class Receipt {
	private static final String RECEIPT_ID_KEY 	= "receiptId";
	
	private JSONObject record;
	
	public Receipt(String record) {
		this.record = JSONObject.fromObject(record);
	}
	
	public Receipt(JSONObject record) {
		this.record = record;
	}
	
	public String getReceiptId() {
		if (record.has(RECEIPT_ID_KEY)) {
			return record.getString(RECEIPT_ID_KEY);
		}
		return null;
	}
	
	public String toString() {
		String result = (this.record != null) ? this.record.toString() : null;
		return result;
	}
	
	public static class Builder {
		private String receiptId;
		
		public Builder setReceiptId(String receiptId) {
			this.receiptId = receiptId;
			return this;
		}
		
		public Receipt build() {
			if (this.receiptId == null) {
				return null;
			}
			
			JSONObject record = new JSONObject();
			record.put(RECEIPT_ID_KEY, this.receiptId);
			return new Receipt(record);
		}
	}
}