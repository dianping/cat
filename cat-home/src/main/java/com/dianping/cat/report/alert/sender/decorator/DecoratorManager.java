package com.dianping.cat.report.alert.sender.decorator;

import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.alert.sender.AlertEntity;

public class DecoratorManager extends ContainerHolder implements Initializable {

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		String alertType = alert.getType();
		Decorator decorator = m_decorators.get(alertType);

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
