package com.dianping.cat.report.page.event.task;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;

public class HistoryEventReportMerger extends EventReportMerger {

	double m_duration = 1;

	public HistoryEventReportMerger(EventReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeName(EventName old, EventName other) {
		old.getRanges().clear();
		other.getRanges().clear();
		super.mergeName(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	@Override
	public void visitName(EventName name) {
		name.getRanges().clear();
		super.visitName(name);
	}

	@Override
	public void mergeType(EventType old, EventType other) {
		super.mergeType(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	public HistoryEventReportMerger setDuration(double duration) {
		m_duration = duration;
		return this;
	}
}
