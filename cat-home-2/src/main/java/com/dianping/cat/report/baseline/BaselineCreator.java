package com.dianping.cat.report.baseline;

import java.util.List;

public interface BaselineCreator {
	public double[] createBaseLine(List<double[]> values, List<Double> weights, int number);

}
