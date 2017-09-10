package com.dianping.cat.consumer.core;

import java.text.DecimalFormat;
import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;

public class NumberFormatTest {
	private void checkFormat(Number number, String format, String expected) {
		String actual = new DecimalFormat(format).format(number);

		Assert.assertEquals(expected, actual);
	}

	private void checkParse(String str, String format, Number expected) throws ParseException {
		Number actual = new DecimalFormat(format).parse(str);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testFormat() {
		checkFormat(12, "0", "12");
		checkFormat(12.34, "0", "12");

		checkFormat(12, "0.#", "12");
		checkFormat(12.34, "0.#", "12.3");

		checkFormat(12.34, "0.##", "12.34");
		checkFormat(12.346, "0.##", "12.35");

		checkFormat(0.3467, "0.#%", "34.7%");
	}

	@Test
	public void testParse() throws ParseException {
		checkParse("12", "0", 12L);

		checkParse("12", "0.#", 12L);
		checkParse("12.3", "0.#", 12.3);
		checkParse("12.4", "0.#", 12.4);

		checkParse("12.34", "0.##", 12.34);
		checkParse("12.35", "0.##", 12.35);

		checkParse("34.5%", "0.#%", 0.345);
	}
}
