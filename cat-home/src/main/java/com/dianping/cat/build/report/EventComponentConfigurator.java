package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.event.EventContactor;
import com.dianping.cat.report.alert.event.EventDecorator;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.page.event.service.CompositeEventService;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.event.service.HistoricalEventService;
import com.dianping.cat.report.page.event.service.LocalEventService;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class EventComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(EventRuleConfigManager.class));

		all.add(C(Contactor.class, EventContactor.ID, EventContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Decorator.class, EventDecorator.ID, EventDecorator.class));
		all.add(A(EventAlert.class));

		all.add(A(EventReportService.class));

		all.add(A(LocalEventService.class));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(EventReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, EventAnalyzer.ID, CompositeEventService.class) //
		      .req(ServerConfigManager.class, RemoteServersManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(A(EventReportBuilder.class));

		return all;
	}
}
