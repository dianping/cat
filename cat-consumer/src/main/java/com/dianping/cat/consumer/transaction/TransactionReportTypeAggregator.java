package com.dianping.cat.consumer.transaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionReportTypeAggregator extends BaseVisitor {

	private TransactionReport m_report;

	public String m_currentDomain;

	public static Set<String> ALL_TYPES = new HashSet<String>(Arrays.asList( //
	      "URL", "Call", "PigeonCall", "Service", //
	      "PigeonService", "SQL", "MsgProduceTried", "MsgProduced"));

	public TransactionReportTypeAggregator(TransactionReport report) {
		m_report = report;
	}

	private void mergeType(TransactionType old, TransactionType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());

		if (totalCountSum > 0) {
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value() * other.getTotalCount();

			old.setLine95Value(line95Values / totalCountSum);
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