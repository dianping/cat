package com.dianping.cat.report.page.model.problem;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportService;

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
		return m_reportService.queryProblemReport(domain, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(timestamp, ProblemAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				bucket.close();
			}
		}
	}
}
