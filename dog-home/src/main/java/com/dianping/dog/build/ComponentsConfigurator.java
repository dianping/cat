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
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.alarm.rule.message.MessageCreaterFactory;
import com.dianping.dog.alarm.strategy.AlarmStrategyFactory;
import com.dianping.dog.event.DefaultEventDispatcher;
import com.dianping.dog.event.DefaultEventListenerRegistry;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListenerRegistry;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultEventQueue.class,DefaultEventQueue.class).is(PER_LOOKUP));
		
		all.add(C(MessageCreaterFactory.class,MessageCreaterFactory.class));
		
		all.add(C(AlarmStrategyFactory.class,AlarmStrategyFactory.class));
		
		
		all.add(C(EventListenerRegistry.class, DefaultEventListenerRegistry.class));
		all.add(C(EventDispatcher.class, DefaultEventDispatcher.class) //
		      .req(EventListenerRegistry.class));
		all.add(C(ProblemFilterListener.class, ProblemFilterListener.class)
				.req(EventDispatcher.class));
		
		all.add(C(RuleManager.class,DefaultRuleManager.class)
				.req(EventDispatcher.class));
		
		all.add(C(ProblemMergeListener.class, ProblemMergeListener.class)
				.req(DefaultEventQueue.class).req(RuleManager.class));	
		
		all.add(C(AlertEventListener.class,AlertEventListener.class)
				.req(MessageCreaterFactory.class)
				.req(AlarmStrategyFactory.class));
		
		
		all.add(C(DataParserFactory.class,DataParserFactory.class));
		all.add(C(ConnectorManager.class, ConnectorManager.class)
				.req(DataParserFactory.class));
		all.add(C(DataService.class, DataService.class)
				.req(ConnectorManager.class)
				.req(EventDispatcher.class));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
