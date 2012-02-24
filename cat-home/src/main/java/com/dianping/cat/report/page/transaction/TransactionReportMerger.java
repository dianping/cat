package com.dianping.cat.report.page.transaction;

import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;

public class TransactionReportMerger extends DefaultMerger {
	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);
	}

	public TransactionReport mergesFrom(TransactionReport report) {
		report.accept(this);

		return getTransactionReport();
	}

	public static TransactionReport merges(List<TransactionReport> reports) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(""));

		for (TransactionReport report : reports) {
			report.accept(merger);
		}

		return merger.getTransactionReport();
	}

	@Override
	protected void mergeName(TransactionName old, TransactionName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2()));
		}
	}

	@Override
	protected void mergeType(TransactionType old, TransactionType other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2()));
		}
	}

	protected double std(long count, double ave, double sum2) {
		return Math.sqrt(sum2 / count - 2 * ave * ave + ave * ave);
	}
}
