package com.dianping.cat.report.page.model.state;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalStateService extends BaseLocalModelService<StateReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	protected StateReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		StateReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			report = getReportFromLocalDisk(request.getStartTime(), domain);
		}
		return report;
	}

	private StateReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, StateAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				bucket.close();
			}
		}
	}
}
