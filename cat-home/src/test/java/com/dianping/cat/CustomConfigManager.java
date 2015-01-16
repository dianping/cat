package com.dianping.cat;

import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.dianping.cat.configuration.client.entity.Domain;

public class CustomConfigManager extends DefaultClientConfigManager {

	@Override
	public Domain getDomain() {
		Domain domain = super.getDomain();
		domain.setId("testDomain");

		return domain;
	}

}
