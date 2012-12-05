package com.dianping.cat.message.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import org.unidal.lookup.annotation.Inject;

public class DefaultMessageConsumerRegistry implements MessageConsumerRegistry, Initializable {
	@Inject
	private List<MessageConsumer> m_consumers = new ArrayList<MessageConsumer>();

	@Override
	public List<MessageConsumer> getConsumers() {
		return m_consumers;
	}

	@Override
	public void registerConsumer(MessageConsumer consumer) {
		m_consumers.add(consumer);
	}

	@Override
	public void initialize() throws InitializationException {
		// a workaround to Plexus ComponentList bug
		m_consumers = new ArrayList<MessageConsumer>(m_consumers);
	}
}
