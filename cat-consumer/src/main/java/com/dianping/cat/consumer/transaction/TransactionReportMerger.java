package com.dianping.cat.consumer.transaction;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;

public class TransactionReportMerger extends DefaultMerger {
	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);
	}

	@Override
	public void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
		old.setValue(duration.getValue());
	}

	@Override
	public void mergeMachine(Machine old, Machine machine) {
	}

	@Override
	public void mergeName(TransactionName old, TransactionName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

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
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	public Machine mergesForAllMachine(TransactionReport report) {
		Machine all = new Machine(Constants.ALL);

		for (Machine m : report.getMachines().values()) {
			if (!m.getIp().equals(Constants.ALL)) {
				visitMachineChildren(all, m);
			}
		}

		return all;
	}

	@Override
	public void mergeType(TransactionType old, TransactionType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();
		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
			double line99Values = old.getLine99Value() * old.getTotalCount() + other.getLine99Value()
			      * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
			old.setLine99Value(line99Values / totalCountSum);
		}

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

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
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		getTransactionReport().getDomainNames().addAll(transactionReport.getDomainNames());
		getTransactionReport().getIps().addAll(transactionReport.getIps());
	}
}
