package com.dianping.cat.report.page.model.database;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalDatabaseService extends BaseLocalModelService<DatabaseReport> {
	@Inject
	private BucketManager m_bucketManager;

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
				report = new DatabaseReport(database);
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeUtil.ONE_HOUR - 1));
			}
		}

		return report;
	}
}
