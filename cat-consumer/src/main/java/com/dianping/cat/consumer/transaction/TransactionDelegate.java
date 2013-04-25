package com.dianping.cat.consumer.transaction;

import java.util.Date;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;

public class TransactionDelegate implements ReportDelegate<TransactionReport> {
	@Override
	public TransactionReport parse(String xml) throws Exception {
		TransactionReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
	public TransactionReport make(String domain, long startTime, long duration) {
		TransactionReport report = new TransactionReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TransactionReport merge(TransactionReport old, TransactionReport other) {
		TransactionReportMerger merger = new TransactionReportMerger(old);

		other.accept(merger);
		return old;
	}
}
