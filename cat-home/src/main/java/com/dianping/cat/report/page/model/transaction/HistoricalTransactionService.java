package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private BucketManager m_bucketManager;

	public HistoricalTransactionService() {
		super("transaction");
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(new Date(date), getName(), "remote");

			String xml = bucket.findById(domain);

			if (xml != null) {
				TransactionReport report = new DefaultXmlParser().parse(xml);

				return report;
			} else {
				return null;
			}
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
