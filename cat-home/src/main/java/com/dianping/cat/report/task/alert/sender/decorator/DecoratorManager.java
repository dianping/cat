package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class DecoratorManager implements Initializable {

	@Inject(type = Decorator.class, value = MailDecorator.ID)
	private Decorator m_mailDecorator;

	@Inject(type = Decorator.class, value = WeixinDecorator.ID)
	private Decorator m_weixinDecorator;

	@Inject(type = Decorator.class, value = SmsDecorator.ID)
	private Decorator m_smsDecorator;

	private Map<String, Decorator> m_decorators = new HashMap<String, Decorator>();

	public Pair<String, String> generateTitleAndContent(AlertEntity alert, String channelName) {
		Decorator decorator = m_decorators.get(channelName);
		String title = decorator.generateTitle(alert);
		String content = decorator.generateContent(alert);
		
		return new Pair<String, String>(title, content);
	}

	@Override
	public void initialize() throws InitializationException {
		m_decorators.put(m_mailDecorator.getId(), m_mailDecorator);
		m_decorators.put(m_weixinDecorator.getId(), m_weixinDecorator);
		m_decorators.put(m_smsDecorator.getId(), m_smsDecorator);
	}

	public void setMailDecorator(Decorator decorator) {
		m_mailDecorator = decorator;
	}

	public void setSmsDecorator(Decorator decorator) {
		m_smsDecorator = decorator;
	}

	public void setWeixinDecorator(Decorator decorator) {
		m_weixinDecorator = decorator;
	}
}
