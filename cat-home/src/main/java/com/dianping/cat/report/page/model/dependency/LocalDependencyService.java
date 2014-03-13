package com.dianping.cat.report.page.model.dependency;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class LocalDependencyService extends BaseLocalModelService<DependencyReport> {

	@Inject
	private BucketManager m_bucketManager;

	public LocalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected DependencyReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		DependencyReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			report = getReportFromLocalDisk(request.getStartTime(), domain);
		}
		return report;
	}

	private DependencyReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, DependencyAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				bucket.close();
			}
		}
	}
}
