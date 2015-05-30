package com.dianping.cat.report.page.metric.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
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
		List<MetricReport> reports = super.getReport(period, domain);
		MetricReport report = null;

		if (reports != null) {
			report = new MetricReport(domain);
			MetricReportMerger merger = new MetricReportMerger(report);

			for (MetricReport tmp : reports) {
				tmp.accept(merger);
			}
		}

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
		MetricReportFilter filter = new MetricReportFilter(payload.getMin(), payload.getMax());

		return filter.buildXml(report);
	}

	private MetricReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		MetricReport report = new MetricReport(domain);
		MetricReportMerger merger = new MetricReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < ANALYZER_COUNT; i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, MetricAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					MetricReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				}
			} finally {
				if (bucket != null) {
					m_bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	public static class MetricReportFilter extends com.dianping.cat.consumer.metric.model.transform.DefaultXmlBuilder {

		private int m_min;

		private int m_max;

		public MetricReportFilter(int min, int max) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitSegment(Segment segment) {
			int id = segment.getId();

			if (m_min == -1 && m_max == -1) {
				super.visitSegment(segment);
			} else if (id <= m_max && id >= m_min) {
				super.visitSegment(segment);
			}
		}

	}

}
