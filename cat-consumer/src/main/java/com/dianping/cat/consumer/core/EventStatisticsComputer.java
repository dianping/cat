package com.dianping.cat.consumer.core;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventStatisticsComputer extends BaseVisitor {
	@Override
	public void visitName(EventName name) {
		super.visitName(name);

		long count = name.getTotalCount();

		if (count > 0) {
			long failCount = name.getFailCount();
			double failPercent = 100.0 * failCount / count;

			name.setFailPercent(failPercent);
		}
	}

	@Override
	public void visitType(EventType type) {
		super.visitType(type);

		long count = type.getTotalCount();

		if (count > 0) {
			long failCount = type.getFailCount();
			double failPercent = 100.0 * failCount / count;

			type.setFailPercent(failPercent);
		}
	}
}