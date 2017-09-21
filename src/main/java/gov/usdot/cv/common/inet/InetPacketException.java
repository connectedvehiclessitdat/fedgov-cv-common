package gov.usdot.cv.common.inet;

public class InetPacketException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InetPacketException(String message) {
		super(message);
	}
	
	public InetPacketException(String message, Throwable cause) {
		super(message, cause);
	}
}
