package com.dianping.cat.consumer.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventTpsStatisticsComputer extends BaseVisitor {

	public double m_duration = 3600;

	public EventTpsStatisticsComputer setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	@Override
	public void visitName(EventName name) {
		if (m_duration > 0) {
			name.setTps(name.getTotalCount() * 1.0 / m_duration);
		}
	}

	@Override
	public void visitType(EventType type) {
		if (m_duration > 0) {
			type.setTps(type.getTotalCount() * 1.0 / m_duration);
			super.visitType(type);
		}
	}
}
