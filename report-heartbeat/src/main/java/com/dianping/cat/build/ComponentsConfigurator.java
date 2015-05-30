package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.heartbeat.analyzer.HeartbeatAnalyzer;
import com.dianping.cat.heartbeat.analyzer.HeartbeatDelegate;
import com.dianping.cat.heartbeat.service.CompositeHeartbeatService;
import com.dianping.cat.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.heartbeat.service.HistoricalHeartbeatService;
import com.dianping.cat.heartbeat.service.LocalHeartbeatService;
import com.dianping.cat.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.task.TaskBuilder;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineHeartbeatComponents());
		all.addAll(defineServiceComponents());
		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineHeartbeatComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = HeartbeatAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, HeartbeatAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, HeartbeatDelegate.class).req(TaskManager.class,
		      ServerFilterConfigManager.class));

		return all;
	}

	private Collection<Component> defineServiceComponents() {
		List<Component> all = new ArrayList<Component>();
		
		all.add(C(HeartbeatReportService.class).req(HourlyReportDao.class, HourlyReportContentDao.class,
		      DailyReportDao.class, DailyReportContentDao.class));

		all.add(C(LocalModelService.class, LocalHeartbeatService.ID, LocalHeartbeatService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "heartbeat-historical", HistoricalHeartbeatService.class) //
		      .req(HeartbeatReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, HeartbeatAnalyzer.ID, CompositeHeartbeatService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "heartbeat-historical" }, "m_services"));
		all.add(C(TaskBuilder.class, HeartbeatReportBuilder.ID, HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, HeartbeatReportService.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
