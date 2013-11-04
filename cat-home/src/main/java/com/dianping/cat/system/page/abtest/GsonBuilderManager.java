package com.dianping.cat.system.page.abtest;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;

public class GsonBuilderManager {
	private GsonBuilder m_gsonBuilder;

	public GsonBuilderManager() {
		m_gsonBuilder = new GsonBuilder();

		m_gsonBuilder.setFieldNamingStrategy(new NonPrexFieldNamingStrategy());
	}

	public GsonBuilder getGsonBuilder() {
		return m_gsonBuilder;
	}

	public class NonPrexFieldNamingStrategy implements FieldNamingStrategy {
		@Override
		public String translateName(java.lang.reflect.Field f) {
			String name = f.getName();
			int pos = name.indexOf('_');

			return name.substring(pos + 1);
		}
	}
}
