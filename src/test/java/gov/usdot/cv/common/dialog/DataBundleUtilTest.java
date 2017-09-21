package gov.usdot.cv.common.dialog;

import static org.junit.Assert.*;

import gov.usdot.cv.common.util.UnitTestHelper;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

public class DataBundleUtilTest {
	
	static final private boolean isDebugOutput = false;

	@BeforeClass
	public static void init() throws Exception {
		UnitTestHelper.initLog4j(isDebugOutput);
	}

	@Test
	public void test() {
		test("localhost", 47651, true,  "some payload text", "some certificate text");
		test("127.0.0.1", 47651, false, "some payload text", "some certificate text");
		test("localhost", 47651, false, "some payload text1", "some certificate text");
		test("2607:f0d0:1002:51::4", 47651, false, "some payload text", "some certificate text1");
		test("2607:f0d0:1002:0051:0000:0000:0000:0004", 47651, false, "some payload text", null);
		
		try { 
			test("localhost", 47651, false, null, "some certificate text"); 
		} catch (Exception ex) {
			assertEquals(ex instanceof NullPointerException, true);
		}
		
		try { 
			test("localhost", 47651, true, null, null);
		} catch (Exception ex) {
			assertEquals(ex instanceof NullPointerException, true);
		}
		
		try { 
			test(null, 0, false, null, null);
		} catch (Exception ex) {
			assertEquals(ex instanceof NullPointerException, true);
		}
	}
	
	public void test(String destHost, int destPort, boolean fromForwarder, String payload, String certificate ) {
		String receiptId = UUID.randomUUID().toString();
		byte[] payloadBytes = payload != null ? payload.getBytes() : null;
		byte[] certificateBytes = certificate != null ? certificate.getBytes() : null;
		
		DataBundle.Builder builder = new DataBundle.Builder();
		builder.setReceiptId(receiptId).setDestHost(destHost).setDestPort(destPort)
			.setFromForwarder(fromForwarder).setPayload(payloadBytes).setCertificate(certificateBytes);
		DataBundle dataBundleIn = builder.build();
		
		String encodedDataBundle = DataBundleUtil.encode(dataBundleIn);
		DataBundle dataBundleOut = DataBundleUtil.decode(encodedDataBundle);
		
		assertEquals(receiptId, dataBundleOut.getReceiptId());
		assertEquals(destHost, dataBundleOut.getDestHost());
		assertEquals(destPort, dataBundleOut.getDestPort());
		assertEquals(fromForwarder, dataBundleOut.fromForwarder());
		assertArrayEquals(payloadBytes, dataBundleOut.getPayload());
		assertArrayEquals(certificateBytes, dataBundleOut.getCertificate());
	}

}
