package com.dianping.dog.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.dog.alarm.rule.DefaultRuleContext;
import com.dianping.dog.alarm.rule.RuleContext;
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

		all.add(C(EventListenerRegistry.class, DefaultEventListenerRegistry.class));
		all.add(C(EventDispatcher.class, DefaultEventDispatcher.class) //
		      .req(EventListenerRegistry.class));
		all.add(C(RuleContext.class, DefaultRuleContext.class).is(PER_LOOKUP));

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
