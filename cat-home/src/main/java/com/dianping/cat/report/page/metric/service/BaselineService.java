package com.dianping.cat.report.page.metric.service;

import java.util.Date;

import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.alert.MetricType;

public interface BaselineService {

	public void insertBaseline(Baseline baseline);

	public double[] queryBaseline(int currentMinute, int ruleMinute, String metricKey, MetricType type);

	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod);

	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod);

	public boolean hasDailyBaseline(String reportName, String key, Date reportPeriod);

}
