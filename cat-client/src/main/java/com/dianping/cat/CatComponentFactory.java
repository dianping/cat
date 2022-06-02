package com.dianping.cat;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.component.ComponentContext.ComponentFactory;
import com.dianping.cat.component.ComponentContext.InstantiationStrategy;
import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.DefaultTransportManager;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.internal.DefaultMessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

@API(status = Status.INTERNAL, since = "3.1.0")
public class CatComponentFactory implements ComponentFactory {
	@Override
	public Object create(Class<?> componentType) {
		if (componentType == ClientConfigManager.class) {
			return new DefaultClientConfigManager();
		} else if (componentType == MessageIdFactory.class) {
			return new MessageIdFactory();
		} else if (componentType == MessageManager.class) {
			return new DefaultMessageManager();
		} else if (componentType == MessageProducer.class) {
			return new DefaultMessageProducer();
		} else if (componentType == TcpSocketSender.class) {
			return new TcpSocketSender();
		} else if (componentType == TransportManager.class) {
			return new DefaultTransportManager();
		} else if (componentType == MessageStatistics.class) {
			return new DefaultMessageStatistics();
		} else if (componentType == StatusUpdateTask.class) {
			return new StatusUpdateTask();
		}

		return null;
	}

	@Override
	public InstantiationStrategy getInstantiationStrategy(Class<?> componentType) {
		return InstantiationStrategy.SINGLETON;
	}
}
