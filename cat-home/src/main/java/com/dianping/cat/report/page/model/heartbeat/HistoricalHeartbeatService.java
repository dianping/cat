package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalHeartbeatService extends BaseHistoricalModelService<HeartbeatReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

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
		return m_reportSerivce.queryHeartbeatReport(domain, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "heartbeat");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
