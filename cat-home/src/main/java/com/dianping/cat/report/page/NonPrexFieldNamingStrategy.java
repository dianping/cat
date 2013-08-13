package com.dianping.cat.report.page;

import com.google.gson.FieldNamingStrategy;

public class NonPrexFieldNamingStrategy implements FieldNamingStrategy {

	@Override
	public String translateName(java.lang.reflect.Field f) {

		String name = f.getName();

		int pos = name.indexOf('_');

		return name.substring(pos + 1);

	}

}