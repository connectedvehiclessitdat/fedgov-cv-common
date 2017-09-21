package gov.usdot.cv.common.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InstanceMetadataUtilTest {
	
	@Test
	public void testGetNodeNumber() {
		System.setProperty("RTWS_FQDN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		System.setProperty("RTWS_DOMAIN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		assertEquals(-1, InstanceMetadataUtil.getNodeNumber());
		
		System.setProperty("RTWS_FQDN", "transport.plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		System.setProperty("RTWS_DOMAIN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		assertEquals(1, InstanceMetadataUtil.getNodeNumber());
		
		System.setProperty("RTWS_FQDN", "transport2.plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		System.setProperty("RTWS_DOMAIN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		assertEquals(2, InstanceMetadataUtil.getNodeNumber());
		
		System.setProperty("RTWS_FQDN", "transport25.plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		System.setProperty("RTWS_DOMAIN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		assertEquals(25, InstanceMetadataUtil.getNodeNumber());
		
		System.setProperty("RTWS_FQDN", "transport1001.plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		System.setProperty("RTWS_DOMAIN", "plugfest-lcsdw.cv-dev.aws-dev.deleidos.com");
		assertEquals(1001, InstanceMetadataUtil.getNodeNumber());
	}
	
}