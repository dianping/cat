package com.dianping.cat.report.page.model.matrix;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalMatrixService extends BaseHistoricalModelService<MatrixReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public HistoricalMatrixService() {
		super("matrix");
	}

	@Override
	protected MatrixReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		MatrixReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private MatrixReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			MatrixReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, "matrix", ReportEntity.READSET_DOMAIN_NAME);

		if (matrixReport != null && historyReports != null) {
			Set<String> domainNames = matrixReport.getDomainNames();
			for (Report report : historyReports) {
				domainNames.add(report.getDomain());
			}
		}
		return merger == null ? null : matrixReport;
	}

	private MatrixReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "matrix");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
