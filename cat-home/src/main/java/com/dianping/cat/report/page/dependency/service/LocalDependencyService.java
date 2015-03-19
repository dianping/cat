package com.dianping.cat.report.page.dependency.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.BasePayload;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalDependencyService extends LocalModelService<DependencyReport> {

	public static final String ID = DependencyAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	public String getReport(ModelRequest request, ModelPeriod period, String domain, BasePayload payload)
	      throws Exception {
		DependencyReport report = super.getReport(period, domain);

		if ((report == null || report.getDomainNames().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return new DependencyReportFilter().buildXml(report);
	}

	private DependencyReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, DependencyAnalyzer.ID);
			String xml = bucket.findById(domain);
			DependencyReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new DependencyReport(domain);
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

	public static class DependencyReportFilter extends
	      com.dianping.cat.consumer.dependency.model.transform.DefaultXmlBuilder {
		public DependencyReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
