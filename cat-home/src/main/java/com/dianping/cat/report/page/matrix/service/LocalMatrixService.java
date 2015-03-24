package com.dianping.cat.report.page.matrix.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
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
	public String buildReport(ModelRequest request, ModelPeriod period, String domain,ApiPayload payload) throws Exception {
		MatrixReport report = super.getReport( period, domain);

		if ((report == null || report.getDomainNames().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return new MatrixReportFilter().buildXml(report);
	}

	private MatrixReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, MatrixAnalyzer.ID);
			String xml = bucket.findById(domain);
			MatrixReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new MatrixReport(domain);
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

	public static class MatrixReportFilter extends com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder {
		public MatrixReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}
}
