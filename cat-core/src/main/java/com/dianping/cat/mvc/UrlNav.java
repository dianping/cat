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

public enum UrlNav {
	SEVEN_DAY_BEFORE("-7d", -24 * 7),

	ONE_DAY_BEFORE("-1d", -24),

	ONE_HOUR_BEFORE("-1h", -1),

	ONE_HOUR_LATER("+1h", 1),

	ONE_DAY_LATER("+1d", 24),

	SEVEN_DAY_LATER("+7d", 24 * 7);

	private int m_hours;

	private String m_title;

	private UrlNav(String title, int hours) {
		m_title = title;
		m_hours = hours;
	}

	public int getHours() {
		return m_hours;
	}

	public String getTitle() {
		return m_title;
	}
}
