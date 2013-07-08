package com.dianping.cat.report.baseline;

import java.io.IOException;
import java.util.Date;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.home.dal.report.Baseline;

public interface BaselineService {

	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) throws DalException, IOException;

	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) throws DalException, IOException;

	public void insertBaseline(Baseline baseline);
}
