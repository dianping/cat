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

import java.util.ArrayList;
import java.util.List;

public enum Level {
	ERROR(3, "ERROR"),
	WARN(2, "WARN"),
	INFO(1, "INFO"),
	DEV(0, "DEV");

	private static List<String> m_levels;

	private int m_code;

	private String m_name;

	private Level(int code, String name) {
		m_code = code;
		m_name = name;
	}

	public static int getCodeByName(String name) {
		for (Level level : Level.values()) {
			if (level.getName().equals(name)) {
				return level.getCode();
			}
		}
		throw new RuntimeException("Invalid level");
	}

	public static String getNameByCode(int code) {
		for (Level level : Level.values()) {
			if (level.getCode() == code) {
				return level.getName();
			}
		}
		throw new RuntimeException("Invalid level");
	}

	public static List<String> getLevels() {
		if (m_levels == null) {
			m_levels = new ArrayList<String>();

			for (Level level : Level.values()) {
				m_levels.add(level.getName());
			}
		}
		return m_levels;
	}

	public int getCode() {
		return m_code;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

}
