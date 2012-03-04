package com.dianping.cat.report.graph;

public interface ValueTranslater {
	public int getMaxValue(double[] values);
	
	public int[] translate(int height, int maxValue, double[] values);
}
