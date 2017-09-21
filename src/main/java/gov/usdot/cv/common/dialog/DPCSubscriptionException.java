package gov.usdot.cv.common.dialog;

import gov.usdot.cv.common.subscription.response.ResponseCode;

public class DPCSubscriptionException extends Exception {

	private static final long serialVersionUID = 1L;

	private long errorCode = 0;
	private String errorText = null;
	private boolean hasError = false;

	public DPCSubscriptionException(String message) {
		super(message);
	}
	
	public DPCSubscriptionException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static DPCSubscriptionException createInstance(String header, long errorCode) {
		String errorText = ResponseCode.getCodeFormattedText((int)errorCode);
		String message = errorText != null ? 
					String.format("%s completed with error: %s (%d).", header, errorText, errorCode) :
					String.format("%s completed with error code %d.", header, errorCode);
		DPCSubscriptionException ex = new DPCSubscriptionException(message);
		ex.hasError  = true;
		ex.errorText = errorText;
		ex.errorCode = errorCode;
		return ex;
	}
	
	public boolean hasError() {
		return hasError;
	}
	
	public long getErrorCode() {
		return errorCode;
	}
	
	public String getErrorText() {
		return errorText;
	}
	
	public static void main(String[] args) {
		long[] errCodes = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 101, 102, 103, 104, 105, 106, 107, 201, 202, 203, 204, 205 };
		for( long errCode : errCodes ) {
			DPCSubscriptionException ex = createInstance( (errCode%2) != 0 ? "Subscribe Request" : "Cancel Request", errCode);
			if ( ex.hasError ) {
				System.out.printf("Error code: %3d, Error text: '%s', \t%s\n", ex.getErrorCode(), ex.getErrorText(), ex.getMessage());
			} else {
				System.out.println("No error code");	
			}
		}
	}
	
}
