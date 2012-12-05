package com.dianping.cat.message.spi.codec;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.spi.codec.WaterfallMessageCodec.Ruler;

public class WaterfallMessageCodecTest {
	@Test
	public void testRuler() {
		checkRuler(0, 0, 1);
		checkRuler(3, 3, 1);
		checkRuler(6, 6, 1);
		checkRuler(10, 10, 1);
		checkRuler(11, 6, 2);
		checkRuler(20, 10, 2);
		checkRuler(21, 7, 3);
		checkRuler(34, 7, 5);
		checkRuler(51, 6, 10);
		checkRuler(100, 10, 10);
		checkRuler(1001, 6, 200);
		checkRuler(3476, 7, 500);
		checkRuler(112819, 6, 20000);
	}

	private void checkRuler(int maxValue, int expectedUnitNum, int expectedUnitStep) {
		Ruler ruler = new Ruler(maxValue);

		Assert.assertEquals(String.format("[%s, %s, %s]", maxValue, expectedUnitNum, expectedUnitStep), ruler.toString());
	}
}
