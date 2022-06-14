package com.dianping.cat.component.factory;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.DefaultConfigureManager;
import com.dianping.cat.configuration.source.AppPropertiesSource;
import com.dianping.cat.configuration.source.ClientXmlSource;
import com.dianping.cat.configuration.source.EnvironmentVariableSource;
import com.dianping.cat.configuration.source.ServerConfigureSource;
import com.dianping.cat.configuration.source.SystemPropertiesSource;
import com.dianping.cat.message.analysis.EventAggregator;
import com.dianping.cat.message.analysis.LocalAggregator;
import com.dianping.cat.message.analysis.TransactionAggregator;
import com.dianping.cat.message.io.DefaultMessageStatistics;
import com.dianping.cat.message.io.DefaultMessageTreePool;
import com.dianping.cat.message.io.MessageSizeControl;
import com.dianping.cat.message.io.MessageStatistics;
import com.dianping.cat.message.io.MessageTreePool;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.tree.MessageEncoder;
import com.dianping.cat.message.tree.MessageIdFactory;
import com.dianping.cat.message.tree.NativeMessageEncoder;
import com.dianping.cat.message.tree.PlainTextMessageEncoder;
import com.dianping.cat.network.ClientTransportManager;
import com.dianping.cat.network.handler.MessageTreeEncoder;
import com.dianping.cat.network.handler.MessageTreeSender;
import com.dianping.cat.status.StatusUpdateTask;

@API(status = Status.INTERNAL, since = "3.1.0")
public class CatComponentFactory extends ComponentFactorySupport {
	@Override
	protected void defineComponents() {
		singletonOf(MessageIdFactory.class);
		singletonOf(TcpSocketSender.class);
		singletonOf(MessageStatistics.class).by(DefaultMessageStatistics.class);
		singletonOf(StatusUpdateTask.class);

		// configure
		singletonOf(ConfigureManager.class).by(DefaultConfigureManager.class);

		singletonOf(ConfigureSource.class, "app-properties").by(AppPropertiesSource.class);
		singletonOf(ConfigureSource.class, "client-xml").by(ClientXmlSource.class);
		singletonOf(ConfigureSource.class, "environment-variable").by(EnvironmentVariableSource.class);
		singletonOf(ConfigureSource.class, "system-properties").by(SystemPropertiesSource.class);
		singletonOf(ConfigureSource.class, "server-configure").by(ServerConfigureSource.class);

		// message
		singletonOf(MessageEncoder.class, PlainTextMessageEncoder.ID).by(PlainTextMessageEncoder.class);
		singletonOf(MessageEncoder.class, NativeMessageEncoder.ID).by(NativeMessageEncoder.class);
		singletonOf(MessageTreePool.class).by(DefaultMessageTreePool.class);
		singletonOf(MessageSizeControl.class);

		// network
		singletonOf(ClientTransportManager.class);
		singletonOf(MessageTreeEncoder.class);
		singletonOf(MessageTreeSender.class);

		// aggregator
		singletonOf(LocalAggregator.class);
		singletonOf(TransactionAggregator.class);
		singletonOf(EventAggregator.class);
	}
}
