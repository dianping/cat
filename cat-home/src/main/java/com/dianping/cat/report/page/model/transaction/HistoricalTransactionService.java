package com.dianping.cat.report.page.model.transaction;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import org.unidal.lookup.annotation.Inject;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

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
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, "transaction", ReportEntity.READSET_DOMAIN_NAME);

		if (transactionReport == null) {
			transactionReport = new TransactionReport(domain);
		}
		Set<String> domainNames = transactionReport.getDomainNames();
		for (Report report : historyReports) {
			domainNames.add(report.getDomain());
		}
		return transactionReport;
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "transaction");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
