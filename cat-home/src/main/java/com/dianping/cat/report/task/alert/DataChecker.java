package com.dianping.cat.report.task.alert;

import java.util.List;

import com.dianping.cat.home.rule.entity.Condition;

public interface DataChecker {
	public AlertResultEntity checkData(double[] value, double[] baseline, List<Condition> conditions);

}
