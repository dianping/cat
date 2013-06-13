package com.dianping.cat.report.task.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.report.page.model.event.EventReportMerger;

public class HistoryEventReportMerger extends EventReportMerger {

	public HistoryEventReportMerger(EventReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeName(EventName old, EventName other) {
		old.getRanges().clear();
		other.getRanges().clear();
		super.mergeName(old, other);
	}

	@Override
	public void visitName(EventName name) {
		name.getRanges().clear();
		super.visitName(name);
	}
}
