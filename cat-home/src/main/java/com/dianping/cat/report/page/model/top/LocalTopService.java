package com.dianping.cat.report.page.model.top;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalTopService extends BaseLocalModelService<TopReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalTopService() {
		super("top");
	}

	private TopReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "top");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected TopReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TopReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);

			if (report == null) {
				Date start = new Date(date);
				Date end = new Date(date + TimeUtil.ONE_HOUR);
				
				report = new TopReport(domain);
				report.setStartTime(start);
				report.setEndTime(end);
			}
		}
		return report;
	}
}
