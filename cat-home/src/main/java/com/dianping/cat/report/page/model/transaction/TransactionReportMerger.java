package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.core.TransactionStatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class TransactionReportMerger extends DefaultMerger {

	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);

		transactionReport.accept(new TransactionStatisticsComputer());
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

		old.setLine95Sum(old.getLine95Sum() + other.getLine95Sum());
		old.setLine95Count(old.getLine95Count() + other.getLine95Count());
		if (old.getLine95Count() > 0) {
			old.setLine95Value(old.getLine95Sum() / old.getLine95Count());
		}
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

	@Override
	public void mergeType(TransactionType old, TransactionType other) {
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

		old.setLine95Sum(old.getLine95Sum() + other.getLine95Sum());
		old.setLine95Count(old.getLine95Count() + other.getLine95Count());
		if (old.getLine95Count() > 0) {
			old.setLine95Value(old.getLine95Sum() / old.getLine95Count());
		}
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
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		getTransactionReport().getDomainNames().addAll(transactionReport.getDomainNames());
		getTransactionReport().getIps().addAll(transactionReport.getIps());
	}

	public double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}
	
	public Machine mergesForAllMachine(TransactionReport report) {
		Machine machine = new Machine(CatString.ALL);

		for (Machine m : report.getMachines().values()) {
			if (!m.getIp().equals(CatString.ALL)) {
				visitMachineChildren(machine, m);
			}
		}

		return machine;
	}

}
