package com.dianping.cat.report.page.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TpsStatistics extends BaseVisitor {

	public double m_duration;

	public TpsStatistics(double duration) {
		m_duration = duration;
	}

	@Override
	public void visitName(TransactionName name) {
		name.setTps(name.getTotalCount() * 1.0 / m_duration);
	}

	@Override
	public void visitType(TransactionType type) {
		type.setTps(type.getTotalCount() * 1.0 / m_duration);
		super.visitType(type);
	}
}
