package com.dianping.cat.alarm.spi.decorator;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;

@Named
public class DecoratorManager extends ContainerHolder implements Initializable {

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		AlertType alertType = alert.getType();
		Decorator decorator = m_decorators.get(alertType.getName());

		if (decorator != null) {
			String title = decorator.generateTitle(alert);
			String content = decorator.generateContent(alert);

			return new Pair<String, String>(title, content);
		} else {
			throw new RuntimeException("error alert type:" + alert.getType());
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_decorators = lookupMap(Decorator.class);
	}

}
