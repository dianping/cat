package com.dianping.cat.consumer.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;

public class FormatTest {
	private void checkFormat(Number number, String format, String expected) {
		String actual = new DecimalFormat(format).format(number);

		Assert.assertEquals(expected, actual);
	}

	private void checkFranceFormat(Number number, String format, String expected) {
		DecimalFormat df = new DecimalFormat(format);

		df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.FRANCE));

		String actual = df.format(number);

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
	public void testFranceFormat() {
		checkFranceFormat(12, "0", "12");
		checkFranceFormat(12.34, "0", "12");

		checkFranceFormat(12, "0.#", "12");
		checkFranceFormat(12.34, "0.#", "12,3");

		checkFranceFormat(12.34, "0.##", "12,34");
		checkFranceFormat(12.346, "0.##", "12,35");

		checkFranceFormat(0.3467, "0.#%", "34,7%");
	}
}
