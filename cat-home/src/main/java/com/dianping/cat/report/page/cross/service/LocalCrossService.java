package com.dianping.cat.report.page.cross.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalCrossService extends LocalModelService<CrossReport> {

	public static final String ID = CrossAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
<<<<<<< HEAD
		CrossReport report = super.getReport(period, domain);
=======
		List<CrossReport> reports = super.getReport(period, domain);
		CrossReport report = null;

		if (reports != null) {
			report = new CrossReport(domain);
			CrossReportMerger merger = new CrossReportMerger(report);

			for (CrossReport tmp : reports) {
				tmp.accept(merger);
			}
		}
>>>>>>> f86721684ccda964204d843c5badb55317c9cd63

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

		}
		return new CrossReportFilter().buildXml(report);
	}

	private CrossReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		CrossReport report = new CrossReport(domain);
		CrossReportMerger merger = new CrossReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < ANALYZER_COUNT; i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, CrossAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					CrossReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				} else {
					report.getDomainNames().addAll(bucket.getIds());
				}
			} finally {
				if (bucket != null) {
					m_bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	public static class CrossReportFilter extends com.dianping.cat.consumer.cross.model.transform.DefaultXmlBuilder {
		public CrossReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
