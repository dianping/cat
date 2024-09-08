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
import com.dianping.cat.message.context.MessageIdFactory;
import com.dianping.cat.message.encoder.MessageTreeEncoder;
import com.dianping.cat.message.encoder.NativeMessageTreeEncoder;
import com.dianping.cat.message.encoder.PlainTextMessageTreeEncoder;
import com.dianping.cat.message.internal.ByteBufQueue;
import com.dianping.cat.message.internal.DefaultByteBufQueue;
import com.dianping.cat.message.io.MessageSizeControl;
import com.dianping.cat.message.pipeline.DefaultMessagePipeline;
import com.dianping.cat.message.pipeline.MessageHandler;
import com.dianping.cat.message.pipeline.MessagePipeline;
import com.dianping.cat.message.pipeline.handler.MessageConveyer;
import com.dianping.cat.message.pipeline.handler.MessageSerializer;
import com.dianping.cat.message.pipeline.handler.MessageTreeSampler;
import com.dianping.cat.message.pipeline.handler.MessageTreeSetHeader;
import com.dianping.cat.message.pipeline.handler.MetricAggregator;
import com.dianping.cat.network.ClientTransportManager;
import com.dianping.cat.network.MessageTransporter;
import com.dianping.cat.status.DefaultMessageStatistics;
import com.dianping.cat.status.MessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

@API(status = Status.INTERNAL, since = "3.1.0")
public class CatComponentFactory extends ComponentFactorySupport {
	@Override
	protected void defineComponents() {
		singletonOf(MessageIdFactory.class);
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
		singletonOf(MessageTreeEncoder.class, PlainTextMessageTreeEncoder.ID).by(PlainTextMessageTreeEncoder.class);
		singletonOf(MessageTreeEncoder.class, NativeMessageTreeEncoder.ID).by(NativeMessageTreeEncoder.class);
		singletonOf(MessageSizeControl.class);

		// pipeline
		singletonOf(MessagePipeline.class).by(DefaultMessagePipeline.class);
		singletonOf(MessageHandler.class, MessageTreeSetHeader.ID).by(MessageTreeSetHeader.class);
		singletonOf(MessageHandler.class, MessageTreeSampler.ID).by(MessageTreeSampler.class);
		singletonOf(MessageHandler.class, MetricAggregator.ID).by(MetricAggregator.class);
		singletonOf(MessageHandler.class, MessageSerializer.ID).by(MessageSerializer.class);
		singletonOf(MessageHandler.class, MessageConveyer.ID).by(MessageConveyer.class);

		// tree
		singletonOf(ByteBufQueue.class).by(DefaultByteBufQueue.class);
		
		// network
		singletonOf(ClientTransportManager.class);
		singletonOf(MessageTransporter.class);
		
		// status
		singletonOf(MessageStatistics.class).by(DefaultMessageStatistics.class);
	}
}
