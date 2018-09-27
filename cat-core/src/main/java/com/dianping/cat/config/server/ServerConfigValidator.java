package com.dianping.cat.config.server;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultValidator;

public class ServerConfigValidator extends DefaultValidator {

	@Override
	public void visitServerConfig(ServerConfig serverConfig) {
		Server defaultServer = serverConfig.findServer(ServerConfigManager.DEFAULT);

		if (defaultServer == null) {
			Server server = new Server(ServerConfigManager.DEFAULT);

			server.setStorage(new StorageConfig());
			server.setConsumer(new ConsumerConfig());
			serverConfig.addServer(server);
		} else {
			if (defaultServer.getStorage() == null) {
				defaultServer.setStorage(new StorageConfig());
			}

			if (defaultServer.getConsumer() == null) {
				defaultServer.setConsumer(new ConsumerConfig());
			}
		}

		super.visitServerConfig(serverConfig);
	}

	@Override
	public void visitConsumer(ConsumerConfig consumer) {
		if (consumer.getLongConfig() == null) {
			consumer.setLongConfig(new LongConfig());
		}

		super.visitConsumer(consumer);
	}
}
