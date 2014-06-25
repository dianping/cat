package com.dianping.cat.report.task.alert;

import java.util.List;

import org.unidal.tuple.Triple;

import com.dianping.cat.home.rule.entity.Condition;

public interface DataChecker {
	public Triple<Boolean, String, String> checkData(double[] value, double[] baseline, List<Condition> conditions);

}
