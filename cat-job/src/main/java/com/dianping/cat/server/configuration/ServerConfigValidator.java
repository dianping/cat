package com.dianping.cat.server.configuration;

import com.dianping.cat.server.configuration.entity.ConsoleConfig;
import com.dianping.cat.server.configuration.entity.ConsumerConfig;
import com.dianping.cat.server.configuration.entity.LongUrl;
import com.dianping.cat.server.configuration.entity.ServerConfig;
import com.dianping.cat.server.configuration.entity.StorageConfig;
import com.dianping.cat.server.configuration.transform.DefaultValidator;

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
		if (consumer.getLongUrl() == null) {
			consumer.setLongUrl(new LongUrl());
		}

		super.visitConsumer(consumer);
	}
}
