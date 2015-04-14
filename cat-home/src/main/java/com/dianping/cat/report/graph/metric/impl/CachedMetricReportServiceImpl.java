package com.dianping.cat.report.graph.metric.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.page.cdn.graph.CdnReportConvertor;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;
import com.dianping.cat.report.page.web.graph.WebReportConvertor;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.IpService;

public class CachedMetricReportServiceImpl implements CachedMetricReportService {

	@Inject
	private MetricReportService m_reportService;

	@Inject
	private ModelService<MetricReport> m_service;

	@Inject
	private IpService m_ipService;

	private final Map<String, MetricReport> m_metricReports = new LinkedHashMap<String, MetricReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, MetricReport> eldest) {
			return size() > 1000;
		}
	};

	private MetricReport getReportFromCache(String product, long date) {
		String key = product + date;
		MetricReport result = m_metricReports.get(key);

		if (result == null) {
			Date start = new Date(date);
			Date end = new Date(date + TimeHelper.ONE_HOUR);

			try {
				result = m_reportService.queryReport(product, start, end);
				m_metricReports.put(key, result);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	@Override
	public MetricReport queryCdnReport(String product, Map<String, String> properties, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, time);

			request.getProperties().putAll(properties);

			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			MetricReport report = getReportFromCache(product, time);
			String cdn = properties.get("cdn");
			String province = properties.get("province");
			String city = properties.get("city");
			CdnReportConvertor cdnReportConvertor = new CdnReportConvertor(m_ipService);

			cdnReportConvertor.setProvince(province).setCity(city).setCdn(cdn);
			cdnReportConvertor.visitMetricReport(report);

			return cdnReportConvertor.getReport();
		}
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

	@Override
	public MetricReport querySystemReport(String product, Map<String, String> properties, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, time);

			request.getProperties().putAll(properties);

			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();
				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			MetricReport report = getReportFromCache(product, time);

			String type = properties.get("type");
			String ipAddrsStr = properties.get("ip");
			Set<String> ipAddrs = null;

			if (!Constants.ALL.equalsIgnoreCase(ipAddrsStr)) {
				String[] ipAddrsArray = ipAddrsStr.split("_");
				ipAddrs = new HashSet<String>(Arrays.asList(ipAddrsArray));
			}
			SystemReportConvertor convert = new SystemReportConvertor(type, ipAddrs);

			convert.visitMetricReport(report);
			return convert.getReport();
		}
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

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			MetricReport report = getReportFromCache(product, time);
			String city = properties.get("city");
			String channel = properties.get("channel");
			String type = properties.get("type");

			WebReportConvertor convert = new WebReportConvertor(type, city, channel);

			convert.visitMetricReport(report);
			return convert.getReport();
		}
	}

}
