package com.dianping.cat.report.page.model.matrix;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalMatrixService extends BaseLocalModelService<MatrixReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalMatrixService() {
		super("matrix");
	}

	@Override
	protected MatrixReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		MatrixReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);
		}

		return report;
	}

	private MatrixReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "matrix");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
