package com.dianping.cat.report.page.model.database;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalDatabaseService extends BaseLocalModelService<DatabaseReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public LocalDatabaseService() {
		super("database");
	}

	private DatabaseReport getLocalReport(long timestamp, String database) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "database");
		String xml = bucket.findById(database);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected DatabaseReport getReport(ModelRequest request, ModelPeriod period, String database) throws Exception {
		DatabaseReport report = super.getReport(request, period, database);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, database);

			if (report == null) {
				report = new DatabaseReport(database);

				List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(date), new Date(
				      date + 60 * 60 * 1000), null, "database", ReportEntity.READSET_DOMAIN_NAME);

				Set<String> databaseNames = report.getDatabaseNames();
				for (Report temp : historyReports) {
					databaseNames.add(temp.getDomain());
				}
			}
		}
		return report;
	}
}
