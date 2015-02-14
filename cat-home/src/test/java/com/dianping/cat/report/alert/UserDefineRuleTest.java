package com.dianping.cat.report.alert;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.tuple.Pair;

public class UserDefineRuleTest {
	@Test
	public void testUserDefineRule() throws Exception {
		String userDefineStr = "import org.unidal.tuple.Pair; import com.dianping.cat.report.alert.RuleType.MonitorRule; public class UserDefinedRule implements MonitorRule{ /* * 请编写checkData()方法, 除了import标准库，其余部分不能改变 * 该方法接受两个参数： values:当前值数组 baselineValue:基线值数组 * 该方法返回一个Pair对象，key是boolean类型，表明是否触发告警； value是String类型，表明告警内容 * 如：没有触发，返回：return new Pair<Boolean, String>(false, \"\"); * 触发报警，返回：return new Pair<Boolean, String>(true, \"alert info\"); */ @Override public Pair<Boolean, String> checkData(double[] values, double[] baselineValues) { if(values.length <= 0){ return new Pair<Boolean, String>(false, \"\"); } else{ return new Pair<Boolean, String>(true, Double.toString(values[0])); } } }";
		RuleType userDefineRuleType = RuleType.getByTypeId(RuleType.UserDefine.getId());
		Pair<Boolean, String> result = userDefineRuleType.executeRule(new double[0], new double[0], userDefineStr);
		Assert.assertFalse(result.getKey());

		double[] array = { 10.0 };
		result = userDefineRuleType.executeRule(array, array, userDefineStr);
		Assert.assertTrue(result.getKey());
		Assert.assertEquals("10.0", result.getValue());
	}
}
