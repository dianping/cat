package com.dianping.cat.system.page.abtest.util;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
	private GsonBuilder m_gsonBuilder;

	public GsonManager() {
		m_gsonBuilder = new GsonBuilder();

		m_gsonBuilder.setFieldNamingStrategy(new NonPrexFieldNamingStrategy());
	}

	public Gson getGson(){
		return m_gsonBuilder.create();
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
