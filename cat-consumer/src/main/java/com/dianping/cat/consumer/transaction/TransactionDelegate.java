package com.dianping.cat.consumer.transaction;

import static com.dianping.cat.report.ReportConstants.ALL;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportConstants;
import com.dianping.cat.report.ReportDelegate;

public class TransactionDelegate implements ReportDelegate<TransactionReport> {
	@Override
	public void afterLoad(Map<String, TransactionReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, TransactionReport> reports) {
		for (TransactionReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}

		TransactionReport all = createAggregatedTypeReport(reports);

		reports.put(all.getDomain(), all);
	}

	@Override
	public String buildXml(TransactionReport report) {
		report.accept(new TransactionStatisticsComputer());

		String xml = new TransactionReportUrlFilter().buildXml(report);

		return xml;
	}

	private TransactionReport createAggregatedTypeReport(Map<String, TransactionReport> reports) {
		TransactionReport first = reports.values().iterator().next();
		TransactionReport all = makeReport(ALL, first.getStartTime().getTime(), ReportConstants.HOUR);
		TransactionReportTypeAggregator visitor = new TransactionReportTypeAggregator(all);

		try {
			for (TransactionReport report : reports.values()) {
				String domain = report.getDomain();

				all.getIps().add(domain);
				all.getDomainNames().add(domain);

				visitor.visitTransactionReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return all;
	}

	@Override
	public String getDomain(TransactionReport report) {
		return report.getDomain();
	}

	@Override
	public TransactionReport makeReport(String domain, long startTime, long duration) {
		TransactionReport report = new TransactionReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TransactionReport mergeReport(TransactionReport old, TransactionReport other) {
		TransactionReportMerger merger = new TransactionReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public TransactionReport parseXml(String xml) throws Exception {
		TransactionReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
