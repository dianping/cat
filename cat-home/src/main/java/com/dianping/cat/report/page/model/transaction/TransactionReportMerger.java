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

	private Machine m_allMachines;

	private boolean m_allName = false;

	private TransactionName m_allNames;

	private String m_ip;

	private String m_type;

	public TransactionReportMerger(TransactionReport transactionReport) {
		super(transactionReport);

		transactionReport.accept(new StatisticsComputer());
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		old.setCount(old.getCount() + duration.getCount());
	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
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
	protected void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
		old.setSum(old.getSum() + range.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	public Machine mergesForAllMachine(TransactionReport report) {
		Machine machine = new Machine(CatString.ALL_IP);

		for (Machine m : report.getMachines().values()) {
			if (!m.getIp().equals(CatString.ALL_IP)) {
				visitMachineChildren(machine, m);
			}
		}

		return machine;
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

	public TransactionReportMerger setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}

	public TransactionReportMerger setAllName(boolean allName) {
		m_allName = allName;
		return this;
	}

	public TransactionReportMerger setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public TransactionReportMerger setType(String type) {
		m_type = type;
		return this;
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

	@Override
	public void visitMachine(Machine machine) {
		if (m_allIp) {
			visitMachineChildren(m_allMachines, machine);
		} else {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		if (m_allName) {
			visitNameChildren(m_allNames, name);
		} else {
			super.visitName(name);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		TransactionReport report = getTransactionReport();

		if (m_allIp) {
			m_allMachines = report.findOrCreateMachine(CatString.ALL_IP);
		}

		if (m_allName) {
			m_allNames = report.findOrCreateMachine(m_ip).findOrCreateType(m_type).findOrCreateName("ALL");
		}

		super.visitTransactionReport(transactionReport);
		report.getDomainNames().addAll(transactionReport.getDomainNames());
		report.getIps().addAll(transactionReport.getIps());
	}


	@Override
	public void visitType(TransactionType type) {
		if (!m_allName || m_allName && m_type.equals(type.getId())) {
			super.visitType(type);
		}
	}
}
