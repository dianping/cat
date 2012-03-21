package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HdfsTransactionService implements ModelService<TransactionReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Override
	public ModelResponse<TransactionReport> invoke(ModelRequest request) {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		ModelResponse<TransactionReport> response = new ModelResponse<TransactionReport>();
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(new Date(date), domain, "remote");

			String xml = bucket.findById(domain);

			if (xml != null) {
				TransactionReport report = new DefaultXmlParser().parse(xml);

				response.setModel(report);
			}
		} catch (Exception e) {
			response.setException(e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}
}
