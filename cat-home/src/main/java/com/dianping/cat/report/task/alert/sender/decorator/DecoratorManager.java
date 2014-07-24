package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class DecoratorManager extends ContainerHolder implements Initializable {

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert, String channelName) {
		Decorator decorator = m_decorators.get(channelName);
		return new Pair<String, String>(decorator.generateTitle(alert), decorator.generateContent(alert));
	}

	@Override
	public void initialize() throws InitializationException {
		m_decorators = lookupMap(Decorator.class);
	}
}
