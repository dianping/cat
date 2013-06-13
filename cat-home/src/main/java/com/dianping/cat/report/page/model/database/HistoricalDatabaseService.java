package com.dianping.cat.report.page.model.database;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalDatabaseService extends BaseHistoricalModelService<DatabaseReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

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
		return m_reportSerivce.queryDatabaseReport(database, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private DatabaseReport getReportFromLocalDisk(long timestamp, String database) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "database");
		String xml = bucket.findById(database);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
