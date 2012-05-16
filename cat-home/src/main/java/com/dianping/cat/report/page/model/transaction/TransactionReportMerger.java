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

	private boolean m_allName = false;

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

	public Machine mergesForAllMachine() {
		Machine machine = new Machine(CatString.ALL_IP);
		TransactionReport report = getTransactionReport();
		for (Machine temp : report.getMachines().values()) {
			mergeMachine(machine, temp);
			visitMachineChildren(machine, temp);
		}
		return machine;
	}

	public TransactionName mergesForAllName() {
		TransactionName name = new TransactionName("ALL");
		TransactionReport report = getTransactionReport();
		TransactionType type = report.getMachines().get(m_ip).findType(m_type);

		if (type != null) {
			for (TransactionName n : type.getNames().values()) {
				mergeName(name, n);
				visitNameChildren(name, n);
			}
		}

		return name;
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
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		if (m_allIp) {
			getTransactionReport().addMachine(mergesForAllMachine());
		}
		if (m_allName) {
			getTransactionReport().getMachines().get(m_ip).getTypes().get(m_type).addName(mergesForAllName());
		}
		getTransactionReport().getDomainNames().addAll(transactionReport.getDomainNames());
		getTransactionReport().getIps().addAll(transactionReport.getIps());
	}
}
