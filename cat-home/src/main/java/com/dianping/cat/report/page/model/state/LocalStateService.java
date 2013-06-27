package com.dianping.cat.report.page.model.state;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalStateService extends BaseLocalModelService<StateReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalStateService() {
		super("state");
	}

	private StateReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "state");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected StateReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		StateReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);

			if (report == null) {
				report = new StateReport(domain);
				report.setStartTime(new Date(date));
				report.setEndTime(new Date(date + TimeUtil.ONE_HOUR - 1));
			}
		}

		return report;
	}
}
