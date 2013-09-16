package com.dianping.cat.system.page.abtest;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;

public class GsonBuilderManager {
	private static GsonBuilder s_gsonBuilder = new GsonBuilder();

	static {
		s_gsonBuilder.setFieldNamingStrategy(new NonPrexFieldNamingStrategy());
	}

	public GsonBuilder getGsonBuilder() {
		return s_gsonBuilder;
	}

	public static class NonPrexFieldNamingStrategy implements FieldNamingStrategy {
		@Override
		public String translateName(java.lang.reflect.Field f) {
			String name = f.getName();
			int pos = name.indexOf('_');

			return name.substring(pos + 1);
		}
	}
}
