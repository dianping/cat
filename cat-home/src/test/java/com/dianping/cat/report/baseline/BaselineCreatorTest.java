package com.dianping.cat.report.baseline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.report.baseline.impl.DefaultBaselineCreator;

public class BaselineCreatorTest {
	@Test
	public void testCreateBaseLine(){
		BaselineCreator creator = new DefaultBaselineCreator();
		List<double[]> valueList = new ArrayList<double[]>();
		double[] values1 = {1};
		valueList.add(values1);
		double[] values2 = {2};
		valueList.add(values2);
		double[] values3 = {3};
		valueList.add(values3);
		double[] values4 = {12.6};
		valueList.add(values4);
		Double[] weights = {1.0,1.0,1.0,1.0};
		List<Double> weightList = Arrays.asList(weights);
		Set<Integer> omittedPoints = new HashSet<Integer>();
		double[] result = creator.createBaseLine(valueList, weightList, omittedPoints,1);
		double[] expectedResult = {2.0};
		Assert.assertArrayEquals(expectedResult, result,0.0001);
	}
}
