package com.dianping.cat.report.page.matrix.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportMerger;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalMatrixService extends LocalModelService<MatrixReport> {

	public static final String ID = MatrixAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalMatrixService() {
		super(MatrixAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		List<MatrixReport> reports = super.getReport(period, domain);
		MatrixReport report = null;

		if (reports != null) {
			report = new MatrixReport(domain);
			MatrixReportMerger merger = new MatrixReportMerger(report);

			for (MatrixReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getDomainNames().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return new MatrixReportFilter().buildXml(report);
	}

	private MatrixReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		MatrixReport report = new MatrixReport(domain);
		MatrixReportMerger merger = new MatrixReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < ANALYZER_COUNT; i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, MatrixAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					MatrixReport tmp = DefaultSaxParser.parse(xml);

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

	public static class MatrixReportFilter extends com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder {
		public MatrixReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
