package com.dianping.cat.system.page.abtest.advisor;

public class ABTestEvaluator {
	
	public float getConfidence(float zscore) {
		if (zscore < -3.89) {
			return 0;
		} else if (zscore > 3.89) {
			return 1;
		}
		
		float ret = 0;
		float temp = -3.89f;
		
		while (temp <= zscore) {
			ret += 0.0001f * fx(temp);
			temp += 0.0001f;
		}
		
		return ret;
	}

	public double getConversionRate(double total, double real) {
		return real / total;
	}

	private float fx(float zscore) {
		float ret = 0;
		double a = 1.0 / Math.sqrt(Math.PI * 2);
		
		a = a * Math.pow(Math.E, -0.5 * Math.pow(zscore, 2));
		ret = (float) a;
		
		return ret;
	}

	/*
	 * 95% confidence interval
	 */
	public double getSampleSize(double crActual, double crExpected) {
		double zscore = 1.65;
		double diff = crActual - crExpected;

		return zscore * zscore * (crActual + crExpected - crActual * crActual - crExpected * crExpected) / (diff * diff);
	}

	public double getStandardError(double conversionRate, double size) {
		return Math.sqrt(conversionRate * (1 - conversionRate) / size);
	}

	public double getZsore(double cr1, double cr2, double se1, double se2) {
		return Math.abs((cr1 - cr2) / Math.sqrt(se1 * se1 + se2 * se2));
	}
}
