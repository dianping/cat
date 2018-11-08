package com.dianping.cat.impl;

import com.dianping.cat.CatPropertyProvider;
import com.dianping.cat.util.Properties.PropertyAccessor;

public class CatPropertyProviderDefaultImpl implements CatPropertyProvider {
	
	private PropertyAccessor<String> config;
	
	public CatPropertyProviderDefaultImpl() {
		super();
		config = com.dianping.cat.util.Properties.forString().fromEnv().fromSystem();
	}

	public String getProperty(final String name, final String defaultValue) {
		return config.getProperty(name, defaultValue);
	}
}
