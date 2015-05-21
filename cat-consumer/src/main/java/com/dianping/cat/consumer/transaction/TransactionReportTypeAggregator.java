package com.dianping.cat.consumer.transaction;

import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionReportTypeAggregator extends BaseVisitor {

	private TransactionReport m_report;

	public String m_currentDomain;

	private String m_currentType;

	private AllReportConfigManager m_configManager;

	public TransactionReportTypeAggregator(TransactionReport report, AllReportConfigManager configManager) {
		m_report = report;
		m_configManager = configManager;
	}

	private void mergeName(TransactionName old, TransactionName other) {
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
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();

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
			double line95Values = old.getLine95Value() * old.getTotalCount() + other.getLine95Value()
			      * other.getTotalCount();
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

	private boolean validateName(String type, String name) {
		return m_configManager.validate(TransactionAnalyzer.ID, type, name);
	}

	private boolean validateType(String type) {
		return m_configManager.validate(TransactionAnalyzer.ID, type);
	}

	@Override
	public void visitName(TransactionName name) {
		if (validateName(m_currentType, name.getId())) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			TransactionType curentType = machine.findOrCreateType(m_currentType);
			TransactionName currentName = curentType.findOrCreateName(name.getId());

			mergeName(currentName, name);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_currentDomain = transactionReport.getDomain();
		m_report.setStartTime(transactionReport.getStartTime());
		m_report.setEndTime(transactionReport.getEndTime());
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		String typeName = type.getId();

		if (validateType(typeName)) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			TransactionType result = machine.findOrCreateType(typeName);

			m_currentType = typeName;
			mergeType(result, type);

			super.visitType(type);
		}
	}
}