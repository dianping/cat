package com.dianping.cat.report.page.model.sql;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalSqlService extends BaseHistoricalModelService<SqlReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

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
		return m_reportSerivce.querySqlReport(domain, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private SqlReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "sql");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
