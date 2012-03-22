package com.dianping.cat.report.page.model.event;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;

public class LocalEventService extends BaseLocalModelService<EventReport> {
	public LocalEventService() {
		super("event");
	}
}
