package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.dianping.cat.report.task.CatReportFacade;
import com.dianping.cat.report.task.DailyTaskProducer;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.TaskConsumer;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.site.initialization.DefaultModuleManager;
import com.site.initialization.Module;
import com.site.initialization.ModuleManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
		      .req(MessageConsumer.class, new String[] { "realtime" }, "m_consumers"));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.add(C(TaskConsumer.class, DefaultTaskConsumer.class) //
		      .req(TaskDao.class, CatReportFacade.class));

		all.add(C(TransactionReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(EventReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(ProblemReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(HeartbeatReportBuilder.class) //
		      .req(GraphDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(DailyTaskProducer.class, DailyTaskProducer.class) //
		      .req(TaskDao.class, ReportDao.class, DailyreportDao.class));

		all.add(C(CatReportFacade.class)//
		      .req(TransactionReportBuilder.class, EventReportBuilder.class, ProblemReportBuilder.class,
		            HeartbeatReportBuilder.class));

		all.addAll(new ServiceComponentConfigurator().defineComponents());

		all.add(C(Module.class, CatHomeModule.ID, CatHomeModule.class));
		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));

		// Please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}