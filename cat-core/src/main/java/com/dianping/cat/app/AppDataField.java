/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.app;

import org.unidal.lookup.util.StringUtils;

public enum AppDataField {
	OPERATOR("operator", "运营商"),

	NETWORK("network", "网络类型"),

	APP_VERSION("app-version", "版本"),

	CONNECT_TYPE("connect-type", "连接类型"),

	PLATFORM("platform", "平台"),

	SOURCE("source", "来源"),

	CITY("city", "城市"),

	CODE("code", "返回码");

	private String m_name;

	private String m_title;

	AppDataField(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public static AppDataField getByName(String name, AppDataField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (AppDataField field : AppDataField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	public static AppDataField getByTitle(String title) {
		if (StringUtils.isNotEmpty(title)) {
			for (AppDataField field : AppDataField.values()) {
				if (field.getTitle().equals(title)) {
					return field;
				}
			}
		}
		return null;
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}

}
