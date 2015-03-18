package com.dianping.cat.report.page.transaction.task;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class HistoryTransactionReportMerger extends TransactionReportMerger {

	public double m_duration = 1;

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
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	@Override
	public void visitName(TransactionName name) {
		name.getDurations().clear();
		name.getRanges().clear();
		super.visitName(name);
	}

	@Override
	public void mergeType(TransactionType old, TransactionType other) {
		super.mergeType(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	public HistoryTransactionReportMerger setDuration(double duration) {
		m_duration = duration;
		return this;
	}

}
