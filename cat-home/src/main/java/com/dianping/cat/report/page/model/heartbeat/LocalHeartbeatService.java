package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalHeartbeatService extends BaseLocalModelService<HeartbeatReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	@Override
	protected HeartbeatReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		HeartbeatReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
			
			if (report == null) {
				report = new HeartbeatReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeUtil.ONE_HOUR - 1));
			}
		}
		return report;
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, HeartbeatAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
