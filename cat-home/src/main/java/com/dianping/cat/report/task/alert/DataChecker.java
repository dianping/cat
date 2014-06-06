package com.dianping.cat.report.task.alert;

import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.home.rule.entity.Config;

public interface DataChecker {
	public Pair<Boolean, String> checkData(double[] value, double[] baseline, List<Config> configs);

}
