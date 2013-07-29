package com.dianping.cat.home.abtest;

import org.junit.Test;

import com.dianping.cat.system.page.abtest.ABTestCaculator;

public class ABTestCaculatorTest {
	
	@Test
	public void test(){
		double cr1 = ABTestCaculator.conversionRate(3668, 211);
		double cr2 = ABTestCaculator.conversionRate(3508, 217);
		double se1 = ABTestCaculator.standardError(cr1, 3668);
		double se2 = ABTestCaculator.standardError(cr2, 3508);
		
		double zscore = ABTestCaculator.zsore(cr1, cr2, se1, se2);
		
		System.out.println(zscore);
		

		System.out.println(ABTestCaculator.confidence(0.7744f));
		
		System.out.println(ABTestCaculator.recommendedSampleSize(0.1, 0.101));
	}

}
