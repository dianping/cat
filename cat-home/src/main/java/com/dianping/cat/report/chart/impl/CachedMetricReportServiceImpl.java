package com.dianping.cat.report.chart.impl;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.CachedMetricReportService;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.userMonitor.UserMonitorConvert;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CachedMetricReportServiceImpl implements CachedMetricReportService {

	@Inject
	private ReportService m_reportService;

	@Inject
	private ModelService<MetricReport> m_service;

	private final Map<String, MetricReport> m_metricReports = new LinkedHashMap<String, MetricReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, MetricReport> eldest) {
			return size() > 500;
		}
	};

	private MetricReport getReportFromCache(String product, long date) {
		String key = product + date;
		MetricReport result = m_metricReports.get(key);

		if (result == null) {
			Date start = new Date(date);
			Date end = new Date(date + TimeUtil.ONE_HOUR);

			try {
				result = m_reportService.queryMetricReport(product, start, end);
				m_metricReports.put(key, result);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	@Override
	public MetricReport queryMetricReport(String product, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, time);

			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			return getReportFromCache(product, time);
		}
	}

	private MetricReport hackForTest(String product, Map<String, String> properties) {
		MetricReport report = null;
		try {
			String content = Files.forIO().readFrom(new File("/tmp/data.txt"), "utf-8");

			report = DefaultSaxParser.parse(content);

			report.setProduct(product);

		} catch (Exception e) {
			e.printStackTrace();
		}

		String city = properties.get("city");
		String channel = properties.get("channel");
		String type = properties.get("type");

		UserMonitorConvert convert = new UserMonitorConvert(type, city, channel);

		convert.visitMetricReport(report);

		return convert.getReport();
	}

	@Override
	public MetricReport queryUserMonitorReport(String product, Map<String, String> properties, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, time);

			request.getProperties().putAll(properties);

			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();

				return hackForTest(product, properties);
				// return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			MetricReport report = getReportFromCache(product, time);

			String city = properties.get("city");
			String channel = properties.get("channel");
			String type = properties.get("type");

			UserMonitorConvert convert = new UserMonitorConvert(type, city, channel);
			// return convert.getReport();
			return hackForTest(product, properties);
		}
	}

}
