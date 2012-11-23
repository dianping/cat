package com.dianping.cat.report.page.model.database;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import org.unidal.lookup.annotation.Inject;

public class HistoricalDatabaseService extends BaseHistoricalModelService<DatabaseReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public HistoricalDatabaseService() {
		super("database");
	}

	@Override
	protected DatabaseReport buildModel(ModelRequest request) throws Exception {
		String database = request.getProperty("database");

		long date = Long.parseLong(request.getProperty("date"));
		DatabaseReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, database);
		} else {
			report = getReportFromDatabase(date, database);
		}

		return report;
	}

	private DatabaseReport getReportFromDatabase(long timestamp, String database) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), database, 2, getName(),
		      ReportEntity.READSET_FULL);
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(database));

		for (Report report : reports) {
			String xml = report.getContent();
			DatabaseReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		DatabaseReport databaseReport = merger.getDatabaseReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, "database", ReportEntity.READSET_DOMAIN_NAME);

		if (databaseReport == null) {
			databaseReport = new DatabaseReport(database);
		}
		Set<String> dataBaseNames = databaseReport.getDatabaseNames();
		for (Report report : historyReports) {
			dataBaseNames.add(report.getDomain());
		}
		return databaseReport;
	}

	private DatabaseReport getReportFromLocalDisk(long timestamp, String database) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "database");
		String xml = bucket.findById(database);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
