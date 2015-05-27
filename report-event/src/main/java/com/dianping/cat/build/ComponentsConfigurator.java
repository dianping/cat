package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.AllReportConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportContentDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportContentDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.event.analyzer.EventAnalyzer;
import com.dianping.cat.event.analyzer.EventDelegate;
import com.dianping.cat.event.service.CompositeEventService;
import com.dianping.cat.event.service.EventReportService;
import com.dianping.cat.event.service.HistoricalEventService;
import com.dianping.cat.event.service.LocalEventService;
import com.dianping.cat.event.task.EventGraphCreator;
import com.dianping.cat.event.task.EventMerger;
import com.dianping.cat.event.task.EventReportBuilder;
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

		all.addAll(defineEventComponents());
		all.addAll(defineServiceComponents());
		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineServiceComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(LocalModelService.class, LocalEventService.ID, LocalEventService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "event-historical", HistoricalEventService.class) //
		      .req(EventReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, EventAnalyzer.ID, CompositeEventService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "event-historical" }, "m_services"));

		all.add(C(EventReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(EventGraphCreator.class));
		
		all.add(C(EventMerger.class));
		
		all.add(C(TaskBuilder.class, EventReportBuilder.ID, EventReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, EventReportService.class)//
		      .req(EventReportService.class).req(EventGraphCreator.class, EventMerger.class));//

		return all;
	}

	private Collection<Component> defineEventComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = EventAnalyzer.ID;

		all.add(C(MessageAnalyzer.class, ID, EventAnalyzer.class).is(PER_LOOKUP).req(ReportManager.class, ID)
		      .req(ReportDelegate.class, ID).req(ServerConfigManager.class, ServerFilterConfigManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, EventDelegate.class).req(TaskManager.class, ServerFilterConfigManager.class,
		      AllReportConfigManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
