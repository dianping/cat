package com.dianping.cat.report.page.model.problem;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalProblemService extends BaseLocalModelService<ProblemReport> {

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected ProblemReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		ProblemReport report = super.getReport(request, period, domain);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return report;
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(timestamp, ProblemAnalyzer.ID);
			String xml = bucket.findById(domain);
			ProblemReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new ProblemReport(domain);
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
}
