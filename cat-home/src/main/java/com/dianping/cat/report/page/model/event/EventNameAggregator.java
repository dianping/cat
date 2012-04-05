package com.dianping.cat.report.page.model.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.DefaultMerger;

public class EventNameAggregator extends DefaultMerger {
	public EventNameAggregator(EventReport eventReport) {
		super(eventReport);
	}

	@Override
	protected void mergeName(EventName old, EventName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	protected void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
	}

	public EventReport mergesFrom(EventReport report) {
		report.accept(this);

		return getEventReport();
	}

	@Override
	public void visitRange(Range range) {
		Object parent = getStack().peek();
		Range old = null;

		if (parent instanceof EventName) {
			EventName name = (EventName) parent;

			old = name.findOrCreateRange(range.getValue());
			mergeRange(old, range);
		}

		visitRangeChildren(old, range);
	}

	public EventName mergesFor(String typeName) {
		EventName name = new EventName("ALL");
		EventReport report = getEventReport();
		EventType type = report.findType(typeName);

		if (type != null) {
			for (EventName n : type.getNames().values()) {
				mergeName(name, n);
				visitNameChildren(name, n);
			}
		}

		return name;
	}
}
