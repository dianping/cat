package com.dianping.cat.report.page.model.storage;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.storage.report.ReportBucket;
import com.dianping.cat.storage.report.ReportBucketManager;

public class LocalStorageService extends BaseLocalModelService<StorageReport> {
	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected StorageReport getReport(ModelRequest request, ModelPeriod period, String id) throws Exception {
		StorageReport report = super.getReport(request, period, id);

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, id);
		}
		return report;
	}

	private StorageReport getReportFromLocalDisk(long timestamp, String id) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, StorageAnalyzer.ID);
			String xml = bucket.findById(id);
			StorageReport report = null;

			if (xml != null) {
				report = DefaultSaxParser.parse(xml);
			} else {
				report = new StorageReport(id);
				report.setStartTime(new Date(timestamp));
				report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));
				report.getIds().addAll(bucket.getIds());
			}
			return report;

		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
