package com.dianping.cat.consumer.core;

import java.util.HashSet;
import java.util.Set;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionReportAllBuilder extends BaseVisitor {

	private TransactionReport m_report;

	public String m_currentDomain;

	public static Set<String> ALL_TYPES = new HashSet<String>();

	static {
		ALL_TYPES.add("URL");
		ALL_TYPES.add("Call");
		ALL_TYPES.add("PigeonCall");
		ALL_TYPES.add("Service");
		ALL_TYPES.add("PigeonService");
		ALL_TYPES.add("SQL");
		ALL_TYPES.add("MsgProduceTried");
		ALL_TYPES.add("MsgProduced");
	}

	public TransactionReportAllBuilder(TransactionReport report) {
		m_report = report;
	}

	private void mergeType(TransactionType old, TransactionType other) {
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

		if (old.getTotalCount() + other.getTotalCount() > 0) {
			old.setLine95Value((old.getTotalCount() * old.getLine95Value() + other.getTotalCount() * other.getLine95Value())
			      / (old.getTotalCount() + other.getTotalCount()));
		}

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
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
		m_currentDomain = transactionReport.getDomain();
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		Machine machine = m_report.findOrCreateMachine(m_currentDomain);
		String typeName = type.getId();

		if (typeName.startsWith("Cache.") || ALL_TYPES.contains(typeName)) {
			TransactionType result = machine.findOrCreateType(typeName);

			mergeType(result, type);
		}
	}
}