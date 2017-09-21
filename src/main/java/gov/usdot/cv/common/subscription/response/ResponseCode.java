package gov.usdot.cv.common.subscription.response;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum ResponseCode {
	// missing data property code, 1 - 100
	DialogIDMissing(1),
	SequenceIDMissing(2),
	SubscriberIdMissing(3),
	CertificateMissing(4),
	TargetHostMissing(5),
	TargetPortMissing(6),
	EndTimeMissing(7),
	TypeMissing(8),
	TypeValueMissing(9),
	RequestIdMissing(10),
	NWPosMissing(11),
	NWLatMissing(12),
	NWLonMissing(13),
	SEPosMissing(14),
	SELatMissing(15),
	SELonMissing(16),
	// invalid data code, 101 - 200
	InvalidDialogID(101),
	InvalidSequenceID(102),
	InvalidVsmType(103), 
	InvalidEndTime(104), 
	InvalidRequestId(105),
	InvalidBoundingBox(106),
	InvalidCertificate(107),
	// data processing and server code, >= 201
	InternalServerError(201),
	OperationNotSupported(202),
	SubscriptionExpired(203),
	DatabaseOperationError(204),
	ResourceLimitReached(205);
	
	private static final Map<Integer, ResponseCode> intToResponseCodeMap = new HashMap<Integer, ResponseCode>();
	static {
	    for (ResponseCode responseCode : ResponseCode.values()) {
	    	intToResponseCodeMap.put(responseCode.getCode(), responseCode);
	    }
	}
	
	public static ResponseCode getResponseCode(int code) {
		return intToResponseCodeMap.get(code);
	}
	
	public static String getCodeFormattedText(int code ) {
		ResponseCode responseCode = ResponseCode.getResponseCode(code);
		if ( responseCode != null ) {
			String responseCodeName = responseCode.name();
			if ( responseCodeName != null )
				return Pattern.compile("((?<=[a-z])[A-Z]|[A-Z](?=[a-z]))").matcher(responseCodeName).replaceAll(" $1").trim();
		}
		return null;
	}
	
	private int code;
	
	private ResponseCode(int code) {
		this.code = code;
	}
		
	public int getCode() { return this.code; }
}