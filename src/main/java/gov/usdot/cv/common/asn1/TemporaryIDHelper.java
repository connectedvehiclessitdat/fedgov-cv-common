package gov.usdot.cv.common.asn1;

import gov.usdot.asn1.generated.j2735.dsrc.TemporaryID;

import java.nio.ByteBuffer;

public class TemporaryIDHelper {
	
	static public TemporaryID toTemporaryID(int temporaryID) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(temporaryID).array();
		return new TemporaryID(bytes);
	}
	
	static public int fromTemporaryID(TemporaryID temporaryId) {
		return ByteBuffer.wrap(temporaryId.byteArrayValue()).getInt();
	}
}
