package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlParser;
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
		HeartbeatReport report = getLocalReport(date, domain);

		// try remote report
		if (report == null && !isLocalMode()) {
			report = getRemoteReport(date, domain);
		}

		return report;
	}

	private HeartbeatReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "heartbeat");
		String xml = bucket.findById(domain);

		return xml == null ? null : new DefaultXmlParser().parse(xml);
	}

	private HeartbeatReport getRemoteReport(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		DefaultXmlParser parser = new DefaultXmlParser();
		DefaultMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model = parser.parse(xml);

			if (merger == null) {
				merger = new DefaultMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger.getHeartbeatReport();
	}
}
