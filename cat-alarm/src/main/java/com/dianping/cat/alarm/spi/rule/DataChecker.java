package com.dianping.cat.alarm.spi.rule;

import java.util.List;

import com.dianping.cat.alarm.rule.entity.Condition;

public interface DataChecker {
	public List<DataCheckEntity> checkData(double[] value, double[] baseline, List<Condition> conditions);

	public List<DataCheckEntity> checkData(double[] value, List<Condition> conditions);

	public List<DataCheckEntity> checkDataForApp(double[] value, List<Condition> checkedConditions);

}
