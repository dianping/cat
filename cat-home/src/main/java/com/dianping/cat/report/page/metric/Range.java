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
package com.dianping.cat.report.page.metric;

public enum Range {
	ONE("1小时", 1),

	TWO("2小时", 2),

	FOUR("4小时", 4),

	SIX("6小时", 6),

	EIGHT("8小时", 8),

	TWELVE("12小时", 12),

	ONE_DAY("24小时", 24),

	TWO_DAY("48小时", 48),;

	private String m_title;

	private int m_duration;

	private Range(String title, int duration) {
		m_title = title;
		m_duration = duration;
	}

	public static Range getByTitle(String title, Range defaultRange) {
		for (Range range : Range.values()) {
			if (range.getTitle().equals(title)) {
				return range;
			}
		}
		return defaultRange;
	}

	public int getDuration() {
		return m_duration;
	}

	public String getTitle() {
		return m_title;
	}
}
