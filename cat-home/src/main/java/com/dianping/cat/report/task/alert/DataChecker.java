package com.dianping.cat.report.task.alert;

import java.util.List;

import com.dianping.cat.home.rule.entity.Condition;

public interface DataChecker {
	public AlertEntity checkData(double[] value, double[] baseline, List<Condition> conditions);

}
