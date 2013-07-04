package com.dianping.cat.report.baseline;

import java.util.List;
import java.util.Set;

public interface BaselineCreator {
	public double[] createBaseLine(List<double[]> values,List<Double> weights,Set<Integer> omittedPoints, int number);

}
