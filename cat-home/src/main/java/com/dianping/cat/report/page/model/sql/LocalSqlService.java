package com.dianping.cat.report.page.model.sql;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalSqlService extends BaseLocalModelService<SqlReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalSqlService() {
		super(SqlAnalyzer.ID);
	}

	@Override
	protected SqlReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		SqlReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			report = getReportFromLocalDisk(request.getStartTime(), domain);
		}
		return report;
	}
	
	private SqlReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, SqlAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				bucket.close();
			}
		}
	}
}
