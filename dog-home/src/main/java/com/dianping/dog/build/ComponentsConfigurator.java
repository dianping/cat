package com.dianping.dog.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.dog.alarm.alert.AlertEventListener;
import com.dianping.dog.alarm.connector.ConnectorManager;
import com.dianping.dog.alarm.connector.DataService;
import com.dianping.dog.alarm.filter.ProblemFilterListener;
import com.dianping.dog.alarm.merge.DefaultEventQueue;
import com.dianping.dog.alarm.merge.ProblemMergeListener;
import com.dianping.dog.alarm.parser.DataParserFactory;
import com.dianping.dog.alarm.rule.DefaultRuleManager;
import com.dianping.dog.alarm.rule.ProblemRuleLoader;
import com.dianping.dog.alarm.rule.RuleLoaderFactory;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.alarm.rule.message.MessageCreaterFactory;
import com.dianping.dog.alarm.strategy.AlarmStrategyFactory;
import com.dianping.dog.dal.RuleinstanceDao;
import com.dianping.dog.dal.RuletemplateDao;
import com.dianping.dog.event.DefaultEventDispatcher;
import com.dianping.dog.event.DefaultEventListenerRegistry;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListenerRegistry;
import com.dianping.dog.notify.config.ConfigContext;
import com.dianping.dog.notify.job.ScheduleJob;
import com.dianping.dog.notify.job.SendReportMailJob;
import com.dianping.dog.notify.job.StandardScheduleJobRunner;
import com.dianping.dog.notify.render.IRender;
import com.dianping.dog.notify.render.VelocityRender;
import com.dianping.dog.notify.report.DefaultContainerHolder;
import com.dianping.dog.notify.report.ReportCreaterRegistry;
import com.dianping.dog.notify.report.StandardReportCreaterRegistry;
import com.dianping.dog.service.CommonService;
import com.dianping.dog.service.CommonServiceImp;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultEventQueue.class, DefaultEventQueue.class).is(PER_LOOKUP));

		all.add(C(MessageCreaterFactory.class, MessageCreaterFactory.class));

		all.add(C(AlarmStrategyFactory.class, AlarmStrategyFactory.class));

		all.add(C(EventListenerRegistry.class, DefaultEventListenerRegistry.class));
		all.add(C(EventDispatcher.class, DefaultEventDispatcher.class) //
		      .req(EventListenerRegistry.class));
		all.add(C(ProblemFilterListener.class, ProblemFilterListener.class).req(EventDispatcher.class));

		all.add(C(ProblemRuleLoader.class, ProblemRuleLoader.class));

		all.add(C(RuleLoaderFactory.class, RuleLoaderFactory.class));

		all.add(C(RuleManager.class, DefaultRuleManager.class).req(EventDispatcher.class, RuleinstanceDao.class,
		      RuletemplateDao.class, RuleLoaderFactory.class));

		all.add(C(ProblemMergeListener.class, ProblemMergeListener.class).req(DefaultEventQueue.class).req(
		      RuleManager.class));

		all.add(C(AlertEventListener.class, AlertEventListener.class).req(MessageCreaterFactory.class)
		      .req(AlarmStrategyFactory.class).req(RuleManager.class));

		all.add(C(DataParserFactory.class, DataParserFactory.class));
		all.add(C(ConnectorManager.class, ConnectorManager.class).req(DataParserFactory.class).req(RuleManager.class));
		all.add(C(DataService.class, DataService.class).req(ConnectorManager.class).req(EventDispatcher.class));

		// config for notify
		all.add(C(ConfigContext.class, ConfigContext.class));
		all.add(C(IRender.class, VelocityRender.class).req(ConfigContext.class));
		all.add(C(CommonService.class,CommonServiceImp.class));

		all.add(C(ReportCreaterRegistry.class, StandardReportCreaterRegistry.class));

		all.add(C(ScheduleJob.class,"MailJob", SendReportMailJob.class)
				.req(ReportCreaterRegistry.class)
				.config(E("defaultReceivers").value("yong.you@dianping.com,yanchun.yang@dianping.com")));

		all.add(C(DefaultContainerHolder.class, DefaultContainerHolder.class));
		
		all.add(C(StandardScheduleJobRunner.class, StandardScheduleJobRunner.class).req(DefaultContainerHolder.class)
		      .req(ConfigContext.class)
		      .req(ScheduleJob.class, new String[] { "MailJob"}, "m_jobs"));
		
	/*	all.add(C(SingleScheduleJobRunner.class, SingleScheduleJobRunner.class).req(DefaultContainerHolder.class)
		      .req(ConfigContext.class).req(ScheduleJob.class));*/

		// Please keep it as last
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.addAll(new WebComponentConfigurator().defineComponents());
		
		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
