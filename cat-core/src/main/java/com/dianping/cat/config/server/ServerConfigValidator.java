package com.dianping.cat.config.server;

import com.dianping.cat.configuration.server.entity.ConsoleConfig;
import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.DefaultValidator;

public class ServerConfigValidator extends DefaultValidator {
	@Override
	public void visitConfig(ServerConfig config) {
		if (config.getStorage() == null) {
			config.setStorage(new StorageConfig());
		}

		if (config.getConsumer() == null) {
			config.setConsumer(new ConsumerConfig());
		}

		if (config.getConsole() == null) {
			config.setConsole(new ConsoleConfig());
		}
		
		super.visitConfig(config);
	}

	@Override
	public void visitConsumer(ConsumerConfig consumer) {
		if (consumer.getLongConfig() == null) {
			consumer.setLongConfig(new LongConfig());
		}

		super.visitConsumer(consumer);
	}
}
