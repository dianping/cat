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
package com.dianping.cat.server;

public enum MetricType {

	AVG("mean", "平均值"),

	SUM("sum", "总和"),

	MAX("max", "最大值"),

	MIN("min", "最小值"),

	COUNT("count", "个数");

	private String m_name;

	private String m_title;

	private MetricType(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public static MetricType getByName(String name, MetricType defaultType) {
		for (MetricType type : values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}

		return defaultType;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

}
