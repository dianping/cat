package com.dianping.cat.configuration.model;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.transform.DefaultValidator;

public class ClientConfigValidator extends DefaultValidator {
	@Override
	public void visitConfig(Config config) {
		if (!"client".equals(config.getMode())) {
			throw new RuntimeException(String.format("Attribute(%)s at path(%s) is required!", "mode", "/config"));
		} else if (config.getApp() == null) {
			throw new RuntimeException(String.format("Element(%s) at path(%s) is required!", "app", "/config"));
		} else if (config.getServers().size() == 0) {
			throw new RuntimeException(String.format("Element(%s) at path(%s) is required!", "servers", "/config"));
		}

		super.visitConfig(config);
	}
}
