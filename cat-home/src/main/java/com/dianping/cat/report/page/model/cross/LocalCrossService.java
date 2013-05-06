package com.dianping.cat.report.page.model.cross;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalCrossService extends BaseLocalModelService<CrossReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalCrossService() {
		super("cross");
	}

	private CrossReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "cross");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected CrossReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		CrossReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long date = current - current % (TimeUtil.ONE_HOUR) - TimeUtil.ONE_HOUR;
			report = getLocalReport(date, domain);

			if (report == null) {
				report = new CrossReport(domain);
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeUtil.ONE_HOUR - 1));
			}
		}

		return report;
	}
}
