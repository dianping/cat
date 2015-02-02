package com.dianping.cat.report.page.model.dependency;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalDependencyService extends BaseLocalModelService<DependencyReport> {

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected DependencyReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		DependencyReport report = super.getReport(request, period, domain);

		if ((report == null || report.getDomainNames().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return report;
	}

	private DependencyReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, DependencyAnalyzer.ID);
			String xml = bucket.findById(domain);
			DependencyReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new DependencyReport(domain);
				report.setStartTime(new Date(timestamp));
				report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));
				report.getDomainNames().addAll(bucket.getIds());
			}
			return report;
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
