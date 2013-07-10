package com.dianping.cat.report.task.metric;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineService;

public class MetricBaselineReportBuilderTest extends ComponentTestCase {

	private MetricBaselineReportBuilder getReportBuilder() throws Exception {
		MetricBaselineReportBuilder builder = lookup(MetricBaselineReportBuilder.class);
		builder.m_baselineConfigManager = new MyBaselineConfigManager();
		builder.m_baselineService = new MyBaselineService();
		return builder;
	}

	private class MyBaselineConfigManager extends BaselineConfigManager {
		@Override
		public BaselineConfig queryBaseLineConfig(String key) {
			BaselineConfig config = new BaselineConfig();
			Integer[] days = { 0, -1, -2, -3, -4 };
			Double[] weights = { 1.0, 1.0, 1.0, 1.0, 1.0 };

			config.setDays(Arrays.asList(days));
			config.setId(1);
			config.setKey(key);
			config.setLowerLimit(100);
			config.setMinValue(0.2);
			config.setTargetDate(1);
			config.setUpperLimit(5);
			config.setWeights(Arrays.asList(weights));
			return config;
		}
	}

	private class MyBaselineService implements BaselineService {

		@Override
		public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
			return null;
		}

		@Override
		public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
			return null;
		}

		@Override
		public void insertBaseline(Baseline baseline) {
			if (baseline.getIndexKey().equals("TuanGouWeb:URL:/index:COUNT")) {
				Assert.assertEquals(2157.2, baseline.getDataInDoubleArray()[0], 0.001);
			} else if (baseline.getIndexKey().equals("TuanGouWeb:URL:/index:SUM")) {
				Assert.assertEquals(5.02429914E8, baseline.getDataInDoubleArray()[0], 0.001);
			} else if (baseline.getIndexKey().equals("TuanGouWeb:URL:/index:AVG")) {
				Assert.assertEquals(235669.78113, baseline.getDataInDoubleArray()[0], 0.001);
			}
		}
	}

	@Test
	public void testBuildDailyReport() throws Exception {
		MetricBaselineReportBuilder builder = getReportBuilder();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		builder.buildDailyReportInternal("metric", "TuanGouWeb:URL:/index", format.parse("2013-07-01 00:00:00"));
	}

}
