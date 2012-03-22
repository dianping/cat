package com.dianping.cat.report.page.model.event;

import java.util.Date;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalEventService extends BaseHistoricalModelService<EventReport> {
	@Inject
	private BucketManager m_bucketManager;

	public HistoricalEventService() {
		super("event");
	}

	@Override
	protected EventReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(new Date(date), getName(), "remote");

			String xml = bucket.findById(domain);

			if (xml != null) {
				EventReport report = new DefaultXmlParser().parse(xml);

				return report;
			} else {
				return null;
			}
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
