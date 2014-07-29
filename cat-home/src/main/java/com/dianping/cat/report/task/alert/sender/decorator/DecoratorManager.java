package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class DecoratorManager implements Initializable {

	@Inject(type = Decorator.class, value = BusinessDecorator.ID)
	private Decorator m_businessDecorator;

	@Inject(type = Decorator.class, value = NetworkDecorator.ID)
	private Decorator m_networkDecorator;

	@Inject(type = Decorator.class, value = ExceptionDecorator.ID)
	private Decorator m_exceptionDecorator;

	@Inject(type = Decorator.class, value = SystemDecorator.ID)
	private Decorator m_systemDecorator;

	@Inject(type = Decorator.class, value = ThirdpartyDecorator.ID)
	private Decorator m_thirdpartyDecorator;

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert) {
		Decorator decorator = m_decorators.get(alert.getType());
		String title = decorator.generateTitle(alert);
		String content = decorator.generateContent(alert);

		return new Pair<String, String>(title, content);
	}

	@Override
	public void initialize() throws InitializationException {
		m_decorators.put(m_businessDecorator.getId(), m_businessDecorator);
		m_decorators.put(m_networkDecorator.getId(), m_networkDecorator);
		m_decorators.put(m_exceptionDecorator.getId(), m_exceptionDecorator);
		m_decorators.put(m_systemDecorator.getId(), m_systemDecorator);
		m_decorators.put(m_thirdpartyDecorator.getId(), m_thirdpartyDecorator);
	}

	public void setBusinessDecorator(Decorator decorator) {
		m_businessDecorator = decorator;
	}

	public void setNetworkDecorator(Decorator decorator) {
		m_networkDecorator = decorator;
	}

	public void setExceptionDecorator(Decorator decorator) {
		m_exceptionDecorator = decorator;
	}

	public void setSystemDecorator(Decorator decorator) {
		m_systemDecorator = decorator;
	}

	public void setThirdpartyDecorator(Decorator decorator) {
		m_thirdpartyDecorator = decorator;
	}

}
