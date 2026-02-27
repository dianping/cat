package com.dianping.cat.configuration.source;

import static com.dianping.cat.CatClientConstants.APP_PROPERTIES;

import java.io.InputStream;
import java.util.Properties;

import com.dianping.cat.configuration.ConfigureSource;
import com.dianping.cat.configuration.model.entity.Domain;

// Component
public class AppPropertiesSource implements ConfigureSource<Domain> {
	@Override
	public Domain getConfig() throws Exception {
		Properties properties = new Properties();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES);

		if (in == null) {
			in = getClass().getClassLoader().getResourceAsStream(APP_PROPERTIES);
		}

		if (in != null) {
			Domain domain = new Domain();

			properties.load(in);

			domain.setName(properties.getProperty("app.name"));
			domain.setTenantToken(properties.getProperty("tenant.token"));

			return domain;
		}

		return null;
	}

	@Override
	public int getOrder() {
		return 310;
	}
}
