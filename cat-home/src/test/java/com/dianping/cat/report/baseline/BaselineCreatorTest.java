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
		double [] values1 = {1,2,3,20};
		List<double[]> valueList = getListFromArray(values1);
		Double[] weights = {1.0,1.0,1.0,1.0};
		List<Double> weightList = Arrays.asList(weights);
		Set<Integer> omittedPoints = new HashSet<Integer>();
		
		double[] result = creator.createBaseLine(valueList, weightList, omittedPoints,1);
		double[] expectedResult = {6.5};
		Assert.assertArrayEquals(expectedResult, result,0.0001);
		
		double[] values2 = {97,99,101,103};
		valueList = getListFromArray(values2);
		result = creator.createBaseLine(valueList, weightList, omittedPoints,1);
		expectedResult[0] = 100;
		Assert.assertArrayEquals(expectedResult, result,0.0001);
		
		double[] values3 = {97,99,101,600};
		valueList = getListFromArray(values3);
		result = creator.createBaseLine(valueList, weightList, omittedPoints,1);
		expectedResult[0] = 99;
		Assert.assertArrayEquals(expectedResult, result,0.0001);
		
		double[] values4 = {19,99,101,600};
		valueList = getListFromArray(values4);
		result = creator.createBaseLine(valueList, weightList, omittedPoints,1);
		expectedResult[0] = 100;
		Assert.assertArrayEquals(expectedResult, result,0.0001);
	}
	
	
	private List<double[]> getListFromArray(double []values){
		List<double[]> result = new ArrayList<double[]>();
		for(double value:values){
			double [] valueItem = new double[1];
			valueItem[0] = value;
			result.add(valueItem);
		}
		return result;
	}
}
