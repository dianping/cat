package com.dianping.cat.report.page.model.event;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalEventService extends BaseLocalModelService<EventReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalEventService() {
		super("event");
	}

	@Override
	protected EventReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		EventReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);
		}

		return report;
	}

	private EventReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "event");
		String xml = bucket.findById(domain);

		return xml == null ? null : new DefaultDomParser().parse(xml);
	}
}
