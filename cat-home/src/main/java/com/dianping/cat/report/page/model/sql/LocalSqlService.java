package com.dianping.cat.report.page.model.sql;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalSqlService extends BaseLocalModelService<SqlReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalSqlService() {
		super("sql");
	}

	private SqlReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "sql");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected SqlReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		SqlReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long date = current - current % (TimeUtil.ONE_HOUR) - TimeUtil.ONE_HOUR;
			report = getLocalReport(date, domain);

			if (report == null) {
				report = new SqlReport(domain);
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeUtil.ONE_HOUR - 1));
			}
		}

		return report;
	}
}
