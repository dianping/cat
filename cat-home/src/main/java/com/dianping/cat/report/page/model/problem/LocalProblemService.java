package com.dianping.cat.report.page.model.problem;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalProblemService extends BaseLocalModelService<ProblemReport> {

	@Inject
	private BucketManager m_bucketManager;

	public LocalProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected ProblemReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		ProblemReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		
			if (report == null) {
				report = new ProblemReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeUtil.ONE_HOUR - 1));
			}
		}
		return report;
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;

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
