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
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportContentDao;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportContentDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.cross.analyzer.CrossAnalyzer;
import com.dianping.cat.cross.analyzer.CrossDelegate;
import com.dianping.cat.cross.analyzer.IpConvertManager;
import com.dianping.cat.cross.service.CompositeCrossService;
import com.dianping.cat.cross.service.CrossReportService;
import com.dianping.cat.cross.service.HistoricalCrossService;
import com.dianping.cat.cross.service.LocalCrossService;
import com.dianping.cat.cross.task.CrossReportBuilder;
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

		all.addAll(defineCrossComponents());
		all.addAll(defineServiceComponents());
		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineServiceComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CrossReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));

		all.add(C(LocalModelService.class, LocalCrossService.ID, LocalCrossService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "cross-historical", HistoricalCrossService.class) //
		      .req(CrossReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, CrossAnalyzer.ID, CompositeCrossService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "cross-historical" }, "m_services"));

		all.add(C(TaskBuilder.class, CrossReportBuilder.ID, CrossReportBuilder.class).req(CrossReportService.class));

		return all;
	}

	private Collection<Component> defineCrossComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(C(IpConvertManager.class));
		all.add(C(MessageAnalyzer.class, ID, CrossAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class, IpConvertManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, CrossDelegate.class).req(TaskManager.class, ServerFilterConfigManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
