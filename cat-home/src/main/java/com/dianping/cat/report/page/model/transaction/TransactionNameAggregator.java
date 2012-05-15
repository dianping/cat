package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;

public class TransactionNameAggregator extends DefaultMerger {
	public TransactionNameAggregator(TransactionReport transactionReport) {
		super(transactionReport);
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
	}

	@Override
	public void visitDuration(Duration duration) {
		Object parent = getStack().peek();
		Duration old = null;

		if (parent instanceof TransactionName) {
			TransactionName name = (TransactionName) parent;

			old = name.findOrCreateDuration(duration.getValue());
			mergeDuration(old, duration);
		}

		visitDurationChildren(old, duration);
	}

	@Override
	public void visitRange(Range range) {
		Object parent = getStack().peek();
		Range old = null;

		if (parent instanceof TransactionName) {
			TransactionName name = (TransactionName) parent;

			old = name.findOrCreateRange(range.getValue());
			mergeRange(old, range);
		}

		visitRangeChildren(old, range);
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

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	protected void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	public TransactionName mergesFor(String typeName, String ip) {
		TransactionName name = new TransactionName("ALL");
		TransactionReport report = getTransactionReport();
		TransactionType type = report.getMachines().get(ip).findType(typeName);

		if (type != null) {
			for (TransactionName n : type.getNames().values()) {
				mergeName(name, n);
				visitNameChildren(name, n);
			}
		}

		return name;
	}

	protected double std(long count, double ave, double sum2) {
		return Math.sqrt(sum2 / count - ave * ave);
	}
}
