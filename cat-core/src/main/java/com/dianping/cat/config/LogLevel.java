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
package com.dianping.cat.config;

public enum LogLevel {

	NORMAL(1, "normal"),

	ERROR(2, "error");

	private int m_id;

	private String m_level;

	private LogLevel(int id, String level) {
		m_id = id;
		m_level = level;
	}

	public static String getName(int id) {
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.getId() == id) {
				return logLevel.getLevel();
			}
		}

		throw new RuntimeException("Invalid level.");
	}

	public static int getId(String level) {
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.getLevel().equalsIgnoreCase(level)) {
				return logLevel.getId();
			}
		}

		throw new RuntimeException("Invalid level.");
	}

	public int getId() {
		return m_id;
	}

	public String getLevel() {
		return m_level;
	}
}
