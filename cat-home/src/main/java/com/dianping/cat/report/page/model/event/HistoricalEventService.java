package com.dianping.cat.report.page.model.event;

import java.util.Date;
import java.util.List;

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

			List<String> xmls = bucket.findAllById(domain);

			EventReportMerger merger = null;

			if (xmls != null) {
				for (String xml : xmls) {
					EventReport model = new DefaultXmlParser().parse(xml);
					if (merger == null) {
						merger = new EventReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
				return merger.getEventReport();
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
