package com.dianping.cat.system.page.abtest;

public class ABTestCaculator {

	public static double conversionRate(double total, double real) {
		return real / total;
	}

	public static double standardError(double conversionRate, double size) {
		return Math.sqrt(conversionRate * (1 - conversionRate) / size);
	}

	public static double zsore(double cr1, double cr2, double se1, double se2) {
		return Math.abs((cr1 - cr2) / Math.sqrt(se1 * se1 + se2 * se2));
	}

	/*
	 * 95% confidence interval
	 */
	public static double recommendedSampleSize(double crActual, double crExpected) {
		double zscore = 1.65;
		double diff = crActual - crExpected;

		return zscore * zscore * (crActual + crExpected - crActual * crActual - crExpected * crExpected) / (diff * diff);
	}

	public static float confidence(float u) {
		float ret = 0;
		if (u < -3.89) {
			return 0;
		} else if (u > 3.89) {
			return 1;
		}
		float temp = -3.89f;
		while (temp <= u) {
			ret += 0.0001f * fx(temp);
			temp += 0.0001f;
		}
		return ret;
	}

	private static float fx(float x) {
		float ret = 0;
		double a = 1.0 / Math.sqrt(Math.PI * 2);
		a = a * Math.pow(Math.E, -0.5 * Math.pow(x, 2));
		ret = (float) a;
		return ret;
	}
}
