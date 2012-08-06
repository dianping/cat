package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalHeartbeatService extends BaseHistoricalModelService<HeartbeatReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public HistoricalHeartbeatService() {
		super("heartbeat");
	}

	@Override
	protected HeartbeatReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		HeartbeatReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private HeartbeatReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, null, ReportEntity.READSET_DOMAIN_NAME);

		if (heartbeatReport == null) {
			heartbeatReport = new HeartbeatReport(domain);
		}
		Set<String> domainNames = heartbeatReport.getDomainNames();
		for (Report report : historyReports) {
			domainNames.add(report.getDomain());
		}

		return heartbeatReport;
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "heartbeat");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
