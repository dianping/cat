package com.dianping.cat.report.page.model.problem;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {
	@Inject
	private ReportDao m_reportDao;

	@Inject
	private BucketManager m_bucketManager;

	public HistoricalProblemService() {
		super("problem");
	}

	@Override
	protected ProblemReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		ProblemReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private ProblemReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		ProblemReportMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model = DefaultSaxParser.parse(xml);

			if (merger == null) {
				merger = new ProblemReportMerger(model);
			} else {
				model.accept(merger);
			}
		}
		ProblemReport problemReport = merger.getProblemReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, null, ReportEntity.READSET_DOMAIN_NAME);

		if (problemReport != null && historyReports != null) {
			Set<String> domainNames = problemReport.getDomainNames();
			for (Report report : historyReports) {
				domainNames.add(report.getDomain());
			}
		}
		return merger == null ? null : problemReport;
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "problem");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
