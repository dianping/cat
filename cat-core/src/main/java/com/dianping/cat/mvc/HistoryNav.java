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
package com.dianping.cat.mvc;

public enum HistoryNav {

	MONTH("month", "-1m", "+1m"),

	WEEK("week", "-1w", "+1w"),

	DAY("day", "-1d", "+1d");

	private String m_last;

	private String m_next;

	private String m_title;

	private HistoryNav(String name, String last, String next) {
		m_title = name;
		m_last = last;
		m_next = next;
	}

	public static HistoryNav getByName(String name) {
		for (HistoryNav nav : HistoryNav.values()) {
			if (nav.getTitle().equalsIgnoreCase(name)) {
				return nav;
			}
		}
		return HistoryNav.DAY;
	}

	public String getLast() {
		return m_last;
	}

	public String getNext() {
		return m_next;
	}

	public String getTitle() {
		return m_title;
	}
}
