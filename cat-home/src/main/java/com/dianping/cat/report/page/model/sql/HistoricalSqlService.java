package com.dianping.cat.report.page.model.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalSqlService extends BaseHistoricalModelService<SqlReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public HistoricalSqlService() {
		super("sql");
	}

	@Override
	protected SqlReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		SqlReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private SqlReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			SqlReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		SqlReport sqlReport = merger.getSqlReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, "sql", ReportEntity.READSET_DOMAIN_NAME);

		if (sqlReport == null) {
			sqlReport = new SqlReport(domain);
		}
		Set<String> domainNames = sqlReport.getDomainNames();
		for (Report report : historyReports) {
			domainNames.add(report.getDomain());
		}
		return sqlReport;
	}

	private SqlReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "sql");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
