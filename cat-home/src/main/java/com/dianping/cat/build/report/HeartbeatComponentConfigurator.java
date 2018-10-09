package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatContactor;
import com.dianping.cat.report.alert.heartbeat.HeartbeatDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.service.CompositeHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.heartbeat.service.HistoricalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class HeartbeatComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(HeartbeatRuleConfigManager.class));

		all.add(A(HeartbeatReportService.class));

		all.add(A(LocalHeartbeatService.class));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(HeartbeatReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class, RemoteServersManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));

		all.add(C(Contactor.class, HeartbeatContactor.ID, HeartbeatContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Decorator.class, HeartbeatDecorator.ID, HeartbeatDecorator.class));

		all.add(A(HeartbeatAlert.class));

		all.add(A(HeartbeatReportBuilder.class));

		return all;
	}
}
