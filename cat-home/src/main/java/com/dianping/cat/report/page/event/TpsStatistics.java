package com.dianping.cat.report.page.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class TpsStatistics extends BaseVisitor {

	public double m_duration;

	public TpsStatistics(double duration) {
		m_duration = duration;
	}

	@Override
	public void visitName(EventName name) {
		name.setTps(name.getTotalCount() * 1.0 / m_duration);
	}

	@Override
	public void visitType(EventType type) {
		type.setTps(type.getTotalCount() * 1.0 / m_duration);
		super.visitType(type);
	}
}
