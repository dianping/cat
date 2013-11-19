package com.dianping.cat.abtest;

import org.junit.Ignore;
import org.junit.Test;

import com.dianping.cat.system.page.abtest.advisor.ABTestEvaluator;

@Ignore
public class ABTestCaculatorTest {

	@Test
	public void test() {
		ABTestEvaluator caculator = new ABTestEvaluator();
		
		double cr1 = caculator.getConversionRate(3668, 211);
		double cr2 = caculator.getConversionRate(3508, 217);
		double se1 = caculator.getStandardError(cr1, 3668);
		double se2 = caculator.getStandardError(cr2, 3508);
		double zscore = caculator.getZsore(cr1, cr2, se1, se2);

		System.out.println(zscore);
		System.out.println(caculator.getConfidence(0.7744f));
		System.out.println(caculator.getSampleSize(0.1, 0.101));
	}

}
