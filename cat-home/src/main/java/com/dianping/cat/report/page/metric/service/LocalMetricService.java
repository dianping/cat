package com.dianping.cat.report.page.metric.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.page.cdn.graph.CdnReportConvertor;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;
import com.dianping.cat.report.page.web.graph.WebReportConvertor;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.service.IpService;

public class LocalMetricService extends LocalModelService<MetricReport> {

	public static final String ID = MetricAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	@Inject
	private IpService m_ipService;

	public LocalMetricService() {
		super(MetricAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		MetricReport report = super.getReport(period, domain);

		if ((report == null || report.getMetricItems().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new MetricReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		String metricType = payload.getMetricType();
		String type = payload.getType();

		if (Constants.METRIC_USER_MONITOR.equals(metricType)) {
			String city = payload.getCity();
			String channel = payload.getChannel();
			WebReportConvertor convert = new WebReportConvertor(type, city, channel);

			convert.visitMetricReport(report);
			report = convert.getReport();
		} else if (Constants.METRIC_SYSTEM_MONITOR.equals(metricType)) {
			String ipAddrsStr = payload.getIpAddress();
			Set<String> ipAddrs = null;

			if (!Constants.ALL.equalsIgnoreCase(ipAddrsStr)) {
				String[] ipAddrsArray = ipAddrsStr.split("_");
				ipAddrs = new HashSet<String>(Arrays.asList(ipAddrsArray));
			}
			SystemReportConvertor convert = new SystemReportConvertor(type, ipAddrs);

			convert.visitMetricReport(report);
			report = convert.getReport();
		} else if (Constants.METRIC_CDN.equals(metricType)) {
			String cdn = payload.getCdn();
			String province = payload.getProvince();
			String city = payload.getCity();
			CdnReportConvertor cdnReportConvertor = new CdnReportConvertor(m_ipService);

			cdnReportConvertor.setProvince(province).setCity(city).setCdn(cdn);
			cdnReportConvertor.visitMetricReport(report);
			report = cdnReportConvertor.getReport();
		}

		return new MetricReportFilter().buildXml(report);
	}

	private MetricReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, MetricAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class MetricReportFilter extends com.dianping.cat.consumer.metric.model.transform.DefaultXmlBuilder {
		public MetricReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
