package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class LocalTransactionService extends BaseLocalModelService<TransactionReport> {
	@Inject
	private BucketManager m_bucketManager;

	public LocalTransactionService() {
		super("transaction");
	}

	private TransactionReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "transaction");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}

	@Override
	protected TransactionReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TransactionReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long current = System.currentTimeMillis();
			long hour = 60 * 60 * 1000;
			long date = current - current % (hour) - hour;
			report = getLocalReport(date, domain);

			if (report == null) {
				report = new TransactionReport(domain);

				TransactionReport catReport = getLocalReport(date, "Cat");
				if (catReport != null) {
					report.getDomainNames().addAll(catReport.getDomainNames());
				}
			}
		}

		return report;
	}
}
