package com.dianping.cat.report.page.model.heartbeat;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalHeartbeatService extends BaseLocalModelService<HeartbeatReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

	public LocalHeartbeatService() {
		super("heartbeat");
	}

	private HeartbeatReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "heartbeat");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected HeartbeatReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		HeartbeatReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long date = current - current % (TimeUtil.ONE_HOUR) - TimeUtil.ONE_HOUR;
			report = getLocalReport(date, domain);

			if (report == null) {
				Date start = new Date(date);
				Date end = new Date(date + TimeUtil.ONE_HOUR);
				report = m_reportSerivce.queryHeartbeatReport(domain, start, end);

				if (report == null) {
					report = new HeartbeatReport(domain);
					Set<String> domains = m_reportSerivce.queryAllDomainNames(start, end, domain);
					Set<String> domainNames = report.getDomainNames();

					domainNames.addAll(domains);
				}
			}
		}

		return report;
	}
}
