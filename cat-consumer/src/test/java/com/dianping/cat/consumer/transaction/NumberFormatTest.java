package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;

public class NumberFormatTest {
	@Test
	public void test() {
		check(12, "0", "12");
		check(12.34, "0", "12");
		
		check(12, "0.#", "12");
		check(12.34, "0.#", "12.3");
		check(12.35, "0.#", "12.4");
		
		check(12.34, "0.##", "12.34");
		check(12.346, "0.##", "12.35");
		
		check(0.3467, "0.#%", "34.7%");
	}

	private void check(Number number, String format, String expected) {
		String actual = new java.text.DecimalFormat(format).format(number);

		Assert.assertEquals(expected, actual);
	}
}
