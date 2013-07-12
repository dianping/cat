package com.dianping.cat.report.task.metric;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.metric.MetricAlert.AlertInfo;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class MetricAlertTest extends ComponentTestCase {
	@Test
	public void testMetricAlert() {
		try {
			MetricAlert alert = lookup(MetricAlert.class);
			ModelService<MetricReport> modelService = new MyModelService();
			alert.m_service = modelService;
			alert.m_baselineConfigManager = new MyBaselineConfigManager();
			alert.m_baselineService = new MyBaselineService();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<AlertInfo> alerts = alert.metricAlert(format.parse("2013-07-10 10:00:00"));
			Assert.assertEquals(1, alerts.size());
			Assert.assertEquals(format.parse("2013-07-10 10:00:00"), alerts.get(0).date);
			Assert.assertEquals("TuanGouWeb:URL:/index", alerts.get(0).metricId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyBaselineService implements BaselineService {

		@Override
		public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
			return null;
		}

		@Override
		public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
			double[] result = { 4935.6, 4866.6, 4988.8, 4875.2, 4847.0, 5185.4, 5616.4, 5727.4, 5529.8, 5487.2, 5416.8,
			      5452.0, 5483.6, 5510.2, 5582.8, 5585.4, 5401.2, 5528.0, 5567.2, 5518.0, 5630.4, 5900.0, 5666.8, 5697.4,
			      5639.6, 5672.6, 5705.2, 5935.4, 5889.0, 5780.0, 5724.8, 5925.4, 5912.2, 5882.6, 5866.8, 5443.0, 5678.2,
			      5436.6, 5391.8, 5777.6, 5725.8, 5276.8, 5401.2, 5340.4, 6110.25, 5159.8, 5045.6, 4919.8, 5080.2, 5220.4,
			      5517.2, 6349.25, 5355.4, 5241.4, 6198.0, 6244.25, 6327.75, 6575.0, 6474.75, 6250.5 };
			return result;
		}

		@Override
		public void insertBaseline(Baseline baseline) {
		}

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
			config.setLowerLimit(1);
			config.setMinValue(1);
			config.setTargetDate(1);
			config.setUpperLimit(1);
			config.setWeights(Arrays.asList(weights));
			return config;
		}
	}

	private class MyModelService implements ModelService<MetricReport> {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public ModelResponse<MetricReport> invoke(ModelRequest request) {
			ModelResponse<MetricReport> response = new ModelResponse<MetricReport>();
			if (!request.getDomain().equals("TuanGou")) {
				return null;
			}
			String xml;
			try {
				xml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");
				MetricReport report = DefaultSaxParser.parse(xml);
				response.setModel(report);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		public boolean isEligable(ModelRequest request) {
			return true;
		}

	}
}
