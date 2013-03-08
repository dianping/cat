package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

	public HistoricalTransactionService() {
		super("transaction");
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		TransactionReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private TransactionReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportSerivce.queryTransactionReport(domain, new Date(timestamp),
		      new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "transaction");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
