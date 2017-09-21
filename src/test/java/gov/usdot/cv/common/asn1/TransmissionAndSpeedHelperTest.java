package gov.usdot.cv.common.asn1;

import static org.junit.Assert.*;

import org.junit.Test;

import gov.usdot.asn1.generated.j2735.dsrc.TransmissionAndSpeed;

public class TransmissionAndSpeedHelperTest {

	@Test
	public void test() {
		final double speed = 60.;
		TransmissionAndSpeed tas = TransmissionAndSpeedHelper.createTransmissionAndSpeed(speed);
		assertTrue(Math.round(TransmissionAndSpeedHelper.getSpeedMph(tas)) == speed);
	}

}
