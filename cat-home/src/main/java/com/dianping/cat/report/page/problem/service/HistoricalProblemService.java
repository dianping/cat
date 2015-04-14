package com.dianping.cat.report.page.problem.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {
	@Inject
	private ReportBucketManager m_bucketManager;

	@Inject
	private ProblemReportService m_reportService;

	public HistoricalProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected ProblemReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		ProblemReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private ProblemReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(timestamp, ProblemAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
