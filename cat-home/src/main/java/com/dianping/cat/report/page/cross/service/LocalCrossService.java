package com.dianping.cat.report.page.cross.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.BasePayload;
import com.dianping.cat.service.LocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalCrossService extends LocalModelService<CrossReport> {

	public static final String ID = CrossAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	public String getReport(ModelRequest request, ModelPeriod period, String domain, BasePayload payload)
	      throws Exception {
		CrossReport report = super.getReport(period, domain);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

		}
		return new CrossReportFilter().buildXml(report);
	}

	private CrossReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, CrossAnalyzer.ID);
			String xml = bucket.findById(domain);
			CrossReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new CrossReport(domain);
				report.setStartTime(new Date(timestamp));
				report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));
				report.getDomainNames().addAll(bucket.getIds());
			}
			return report;
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class CrossReportFilter extends com.dianping.cat.consumer.cross.model.transform.DefaultXmlBuilder {
		public CrossReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
