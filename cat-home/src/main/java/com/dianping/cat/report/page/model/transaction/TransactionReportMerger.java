package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.transaction.StatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class TransactionReportMerger extends DefaultMerger {
	private boolean m_allIp = false;

	public TransactionReportMerger setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}

	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);

		transactionReport.accept(new StatisticsComputer());
	}

	@Override
	public void visitMachine(Machine machine) {
		if (m_allIp) {
			Machine newMachine = new Machine(CatString.ALL_IP);
			for (TransactionType type : machine.getTypes().values()) {
				newMachine.addType(type);
			}
			super.visitMachine(newMachine);
		} else {
			super.visitMachine(machine);
		}
	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
		if (m_allIp) {
			Machine old2 = new Machine(CatString.ALL_IP);
			Machine machine2 = new Machine(CatString.ALL_IP);
			for (TransactionType type : old.getTypes().values()) {
				old2.addType(type);
			}
			for (TransactionType type : machine.getTypes().values()) {
				machine2.addType(type);
			}
			super.mergeMachine(old2, machine2);
		} else {
			super.mergeMachine(old, machine);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		getTransactionReport().getDomainNames().addAll(transactionReport.getDomainNames());
		getTransactionReport().getIps().addAll(transactionReport.getIps());
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
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
	protected void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	public TransactionReport mergesFrom(TransactionReport report) {
		report.accept(this);

		return getTransactionReport();
	}

	@Override
	protected void mergeTransactionReport(TransactionReport old, TransactionReport transactionReport) {
		super.mergeTransactionReport(old, transactionReport);
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
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	protected double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}
}
