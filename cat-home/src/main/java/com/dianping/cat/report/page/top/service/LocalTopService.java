package com.dianping.cat.report.page.top.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.BasePayload;
import com.dianping.cat.service.LocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalTopService extends LocalModelService<TopReport> {

	public static final String ID = TopAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	public String getReport(ModelRequest request, ModelPeriod period, String domain, BasePayload payload)
	      throws Exception {
		TopReport report = super.getReport(period, domain);

		if ((report == null || report.getDomains().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new TopReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		return new TopReportFilter().buildXml(report);
	}

	private TopReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TopAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class TopReportFilter extends com.dianping.cat.consumer.top.model.transform.DefaultXmlBuilder {
		public TopReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
