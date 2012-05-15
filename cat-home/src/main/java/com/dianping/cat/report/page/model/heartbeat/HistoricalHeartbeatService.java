package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultDomParser;
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
	private ReportDao m_reportDao;

	@Inject
	private BucketManager m_bucketManager;

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
		DefaultDomParser parser = new DefaultDomParser();
		HeartbeatReportMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model = parser.parse(xml);

			if (merger == null) {
				merger = new HeartbeatReportMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger == null ? null : merger.getHeartbeatReport();
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "heartbeat");
		String xml = bucket.findById(domain);

		return xml == null ? null : new DefaultDomParser().parse(xml);
	}
}
