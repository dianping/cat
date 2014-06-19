package com.dianping.cat.report.chart.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.CachedMetricReportService;
import com.dianping.cat.report.page.cdn.graph.CdnConfig;
import com.dianping.cat.report.page.cdn.graph.CdnReportConvertor;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;
import com.dianping.cat.report.page.userMonitor.graph.UserMonitorReportConvertor;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CachedMetricReportServiceImpl implements CachedMetricReportService {

	@Inject
	private ReportService m_reportService;

	@Inject
	private ModelService<MetricReport> m_service;
	
	@Inject
	private IpService m_ipService;
	
	@Inject
	private CdnConfig m_cdnConfig;

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

			UserMonitorReportConvertor convert = new UserMonitorReportConvertor(type, city, channel);

			convert.visitMetricReport(report);
			return convert.getReport();
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
			CdnReportConvertor cdnReportConvertor = new CdnReportConvertor();

			cdnReportConvertor.SetConventorParameter(m_cdnConfig, m_ipService, cdn, province, city);
			cdnReportConvertor.visitMetricReport(report);
			
			return cdnReportConvertor.getReport();
		}
	}

}
