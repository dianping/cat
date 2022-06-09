package com.dianping.cat.component.factory;

import com.dianping.cat.analyzer.EventAggregator;
import com.dianping.cat.analyzer.LocalAggregator;
import com.dianping.cat.analyzer.TransactionAggregator;
import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.configuration.ApplicationProperties;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.DefaultConfigureManager;
import com.dianping.cat.configuration.source.AppPropertiesSource;
import com.dianping.cat.configuration.source.ClientXmlSource;
import com.dianping.cat.configuration.source.EnvironmentVariableSource;
import com.dianping.cat.configuration.source.SystemPropertiesSource;
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
public class CatComponentFactory extends ComponentFactorySupport {
	@Override
	protected void defineComponents() {
		singletonOf(ApplicationProperties.class);
		singletonOf(MessageIdFactory.class);
		singletonOf(MessageManager.class).by(DefaultMessageManager.class);
		singletonOf(MessageProducer.class).by(DefaultMessageProducer.class);
		singletonOf(TcpSocketSender.class);
		singletonOf(TransportManager.class).by(DefaultTransportManager.class);
		singletonOf(MessageStatistics.class).by(DefaultMessageStatistics.class);
		singletonOf(StatusUpdateTask.class);

		// configure
		singletonOf(ConfigureManager.class).by(DefaultConfigureManager.class);
		
		singletonOf(ConfigureSource.class, "app-properties").by(AppPropertiesSource.class);
		singletonOf(ConfigureSource.class, "client-xml").by(ClientXmlSource.class);
		singletonOf(ConfigureSource.class, "environment-variable").by(EnvironmentVariableSource.class);
		singletonOf(ConfigureSource.class, "system-properties").by(SystemPropertiesSource.class);
		
		// aggregator
		singletonOf(LocalAggregator.class);
		singletonOf(TransactionAggregator.class);
		singletonOf(EventAggregator.class);
	}
}
