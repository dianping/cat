package com.dianping.cat.report.page.model.transaction;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private ReportDao m_reportDao;

	@Inject
	private BucketManager m_bucketManager;

	public HistoricalTransactionService() {
		super("transaction");
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		TransactionReport report = getLocalReport(date, domain);

		// try remote report
		if (report == null && !isLocalMode()) {
			report = getRemoteReport(date, domain);
		}

		return report;
	}

	private TransactionReport getLocalReport(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "transaction");
		String xml = bucket.findById(domain);
		DefaultXmlParser parser = new DefaultXmlParser();

		return parser.parse(xml);
	}

	private TransactionReport getRemoteReport(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		DefaultXmlParser parser = new DefaultXmlParser();
		TransactionReportMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model = parser.parse(xml);

			if (merger == null) {
				merger = new TransactionReportMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger.getTransactionReport();
	}
}
