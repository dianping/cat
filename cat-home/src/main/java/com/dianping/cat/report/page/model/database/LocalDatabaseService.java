package com.dianping.cat.report.page.model.database;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalDatabaseService extends BaseLocalModelService<DatabaseReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

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
			long date = current - current % (TimeUtil.ONE_HOUR) - TimeUtil.ONE_HOUR;
			report = getLocalReport(date, database);

			if (report == null) {
				Date start = new Date(date);
				Date end = new Date(date + TimeUtil.ONE_HOUR);
				report = m_reportSerivce.queryDatabaseReport(database, start, end);

				if (report == null) {
					report = new DatabaseReport(database);
					Set<String> domains = m_reportSerivce.queryAllDomainNames(start, end, database);
					Set<String> domainNames = report.getDomainNames();

					domainNames.addAll(domains);
				}
			}
		}

		return report;
	}
}
