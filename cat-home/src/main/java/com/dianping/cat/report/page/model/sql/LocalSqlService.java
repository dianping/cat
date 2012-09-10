package com.dianping.cat.report.page.model.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalSqlService extends BaseLocalModelService<SqlReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;
	
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
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);

			if (report == null) {
				report = new SqlReport(domain);
				
				List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(hour), new Date(
						hour + 60 * 60 * 1000), null, "sql", ReportEntity.READSET_DOMAIN_NAME);

				Set<String> domainNames = report.getDomainNames();
				for (Report temp : historyReports) {
					domainNames.add(temp.getDomain());
				}
			}
		}
		return report;
	}
}
