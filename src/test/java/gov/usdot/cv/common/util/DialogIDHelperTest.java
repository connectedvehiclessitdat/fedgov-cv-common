package gov.usdot.cv.common.util;

import static org.junit.Assert.*;

import java.io.IOException;

import gov.usdot.asn1.generated.j2735.semi.DataSubscriptionResponse;
import gov.usdot.asn1.generated.j2735.semi.SemiDialogID;
import gov.usdot.asn1.generated.j2735.semi.ServiceRequest;
import gov.usdot.asn1.generated.j2735.semi.VehSitDataMessage;
import gov.usdot.asn1.j2735.CVSampleMessageBuilder;
import gov.usdot.asn1.j2735.J2735Util;
import gov.usdot.cv.common.asn1.DialogIDHelper;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

public class DialogIDHelperTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		UnitTestHelper.initLog4j(Level.WARN);
	}

	@Test
	public void testPositive() {
		String[] posDialogIDs = {
				"vehSitData",
				"dataSubscription",
				"advSitDataDep",
				"advSitDatDist",
				"objReg",
				"objDisc",
				"intersectionSitDataDep",
				"intersectionSitDataQuery",		
		};
		for( String dialogID : posDialogIDs)
			testPositive(dialogID);
	}
	
	@Test
	public void testNegative() {
		String[] negDialogIDs = {
				null,
				"",
				"bogus"
		};
		for( String dialogID : negDialogIDs)
			testNegative(dialogID);
	}
	
	public void testPositive(String dialogID) {
		SemiDialogID dlgID = DialogIDHelper.getDialogID(dialogID);
		assertEquals(DialogIDHelper.getDialogID(dlgID), dialogID);
	}
	
	public void testNegative(String dialogID) {
		SemiDialogID dlgID = DialogIDHelper.getDialogID(dialogID);
		assertNull(dlgID);
	}
	
	@Test
	public void test() throws IOException {
		SemiDialogID[] dialogIDs = {
				SemiDialogID.vehSitData,
				SemiDialogID.dataSubscription,
				SemiDialogID.advSitDataDep,
				SemiDialogID.advSitDatDist,
				SemiDialogID.objReg,
				SemiDialogID.objDisc,
				SemiDialogID.intersectionSitDataDep,
				SemiDialogID.intersectionSitDataQuery,
		};
		for( SemiDialogID dialogID : dialogIDs)
			test(dialogID);
		
		DataSubscriptionResponse dpcRequest = CVSampleMessageBuilder.buildDataSubscriptionResponse();
		assertEquals(DialogIDHelper.getDialogID(dpcRequest).longValue(),dpcRequest.getDialogID().longValue());
		
		VehSitDataMessage vsdm = CVSampleMessageBuilder.buildVehSitDataMessage();
		assertEquals(DialogIDHelper.getDialogID(vsdm).longValue(),vsdm.getDialogID().longValue());
	}
	
	public void test(SemiDialogID dialogID) {
		ServiceRequest request = CVSampleMessageBuilder.buildServiceRequest(J2735Util.createTemporaryID(), dialogID);
		assertEquals(DialogIDHelper.getDialogID(request).longValue(),dialogID.longValue());
	}

}
