package com.dianping.cat.report.page.query.display;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public  class TransactionReportVisitor extends BaseVisitor {
	private String m_type;

	private String m_name;

	private String m_currentType;

	private String m_currentName;

	public TransactionQueryItem m_item = new TransactionQueryItem();

	public TransactionReportVisitor(String type, String name) {
		m_type = type;
		m_name = name;
		m_item.setType(type);
		m_item.setName(name);
	}

	public TransactionQueryItem getItem() {
		return m_item;
	}

	public void setItem(TransactionQueryItem item) {
		m_item = item;
	}

	@Override
	public void visitName(TransactionName name) {
		m_currentName = name.getId();
		if (m_type.equalsIgnoreCase(m_currentType) && m_name.equalsIgnoreCase(m_currentName)) {
			m_item.setTotalCount(name.getTotalCount());
			m_item.setFailCount(name.getFailCount());
			m_item.setFailPercent(name.getFailPercent());
			m_item.setMin(name.getMin());
			m_item.setMax(name.getMax());
			m_item.setAvg(name.getAvg());
			m_item.setLine95Value(name.getLine95Value());
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
		m_item.setDate(transactionReport.getStartTime());
	}

	@Override
	public void visitType(TransactionType type) {
		m_currentType = type.getId();
		if (m_name == null || m_name.trim().length() == 0) {
			if (m_type.equalsIgnoreCase(m_currentType)) {
				m_item.setTotalCount(type.getTotalCount());
				m_item.setFailCount(type.getFailCount());
				m_item.setFailPercent(type.getFailPercent());
				m_item.setMin(type.getMin());
				m_item.setMax(type.getMax());
				m_item.setAvg(type.getAvg());
				m_item.setLine95Value(type.getLine95Value());
			}
		} else {
			super.visitType(type);
		}
	}

}
