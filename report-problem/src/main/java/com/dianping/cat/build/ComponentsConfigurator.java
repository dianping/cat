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
import com.dianping.cat.problem.analyzer.DefaultProblemHandler;
import com.dianping.cat.problem.analyzer.LongExecutionProblemHandler;
import com.dianping.cat.problem.analyzer.ProblemAnalyzer;
import com.dianping.cat.problem.analyzer.ProblemDelegate;
import com.dianping.cat.problem.analyzer.ProblemHandler;
import com.dianping.cat.problem.service.CompositeProblemService;
import com.dianping.cat.problem.service.HistoricalProblemService;
import com.dianping.cat.problem.service.LocalProblemService;
import com.dianping.cat.problem.service.ProblemReportService;
import com.dianping.cat.problem.task.ProblemGraphCreator;
import com.dianping.cat.problem.task.ProblemMerger;
import com.dianping.cat.problem.task.ProblemReportBuilder;
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

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		all.addAll(defineServiceComponents());
		all.addAll(defineProblemComponents());
		return all;
	}

	private Collection<Component> defineServiceComponents() {
		final List<Component> all = new ArrayList<Component>();

		all.add(C(ProblemReportService.class).req(HourlyReportDao.class, DailyReportDao.class, WeeklyReportDao.class,
		      MonthlyReportDao.class, HourlyReportContentDao.class, DailyReportContentDao.class,
		      WeeklyReportContentDao.class, MonthlyReportContentDao.class));
		all.add(C(LocalModelService.class, LocalProblemService.ID, LocalProblemService.class) //
		      .req(ReportBucketManager.class, MessageConsumer.class, ServerConfigManager.class));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
		      .req(ProblemReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, ProblemAnalyzer.ID, CompositeProblemService.class) //
		      .req(ServerConfigManager.class) //
		      .req(ModelService.class, new String[] { "problem-historical" }, "m_services"));
		all.add(C(ProblemGraphCreator.class));

		all.add(C(ProblemMerger.class));
		all.add(C(TaskBuilder.class, ProblemReportBuilder.ID, ProblemReportBuilder.class) //
		      .req(GraphDao.class, DailyGraphDao.class, ProblemReportService.class)//
		      .req(ProblemGraphCreator.class, ProblemMerger.class));

		return all;

	}

	private Collection<Component> defineProblemComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = ProblemAnalyzer.ID;

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
		      .config(E("errorType").value("Error,RuntimeException,Exception"))//
		      .req(ServerConfigManager.class));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
		      .req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
		      .req(ReportManager.class, ID).req(ServerConfigManager.class).req(ProblemHandler.class, //
		            new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
		      .req(ReportDelegate.class, ID) //
		      .req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
		      .config(E("name").value(ID)));
		all.add(C(ReportDelegate.class, ID, ProblemDelegate.class) //
		      .req(TaskManager.class, ServerFilterConfigManager.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
