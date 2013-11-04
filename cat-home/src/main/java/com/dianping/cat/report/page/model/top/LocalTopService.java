package com.dianping.cat.report.page.model.top;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalTopService extends BaseLocalModelService<TopReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	protected TopReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TopReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			report = getReportFromLocalDisk(request.getStartTime(), domain);
		}
		return report;
	}

	private TopReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, TopAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			bucket.close();
		}
	}
}
