package com.dianping.cat.report.baseline;

import java.util.Date;

import com.dianping.cat.home.dal.report.Baseline;

public interface BaselineService {

	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod);

	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod);

	public void insertBaseline(Baseline baseline);
}
