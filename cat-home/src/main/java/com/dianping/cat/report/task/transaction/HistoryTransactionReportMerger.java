package com.dianping.cat.report.task.transaction;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class HistoryTransactionReportMerger extends TransactionReportMerger {

	public HistoryTransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);
	}

	@Override
	public void mergeName(TransactionName old, TransactionName other) {
		old.getDurations().clear();
		old.getRanges().clear();

		other.getDurations().clear();
		other.getRanges().clear();

		super.mergeName(old, other);
	}

	@Override
	public void visitName(TransactionName name) {
		name.getDurations().clear();
		name.getRanges().clear();
		super.visitName(name);
	}

}
