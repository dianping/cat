package com.dianping.cat.impl;

import org.unidal.helper.Properties;
import org.unidal.helper.Properties.PropertyAccessor;

import com.dianping.cat.CatPropertyProvider;

public class CatPropertyProviderDefaultImpl implements CatPropertyProvider {
	
	private PropertyAccessor<String> config;
	
	public CatPropertyProviderDefaultImpl() {
		super();
		config = Properties.forString().fromEnv().fromSystem();
	}
	
	public String getProperty(final String name, final String defaultValue) {
		return config.getProperty(name, defaultValue);
	}
}
