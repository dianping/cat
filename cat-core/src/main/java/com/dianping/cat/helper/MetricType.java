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
package com.dianping.cat.helper;

public enum MetricType {
	COUNT("COUNT", "(次数)"),

	AVG("AVG", "(平均)"),

	SUM("SUM", "(总和)");

	private String m_name;

	private String m_desc;

	MetricType(String name, String desc) {
		m_name = name;
		m_desc = desc;
	}

	public static MetricType getTypeByName(String name) {
		for (MetricType type : MetricType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		throw new RuntimeException("Unsupported MetricType Name!");
	}

	public static String getDesByName(String name) {
		for (MetricType type : MetricType.values()) {
			if (type.getName().equals(name)) {
				return type.getDesc();
			}
		}
		throw new RuntimeException("Unsupported MetricType Name!");
	}

	public String getName() {
		return m_name;
	}

	public String getDesc() {
		return m_desc;
	}
};
