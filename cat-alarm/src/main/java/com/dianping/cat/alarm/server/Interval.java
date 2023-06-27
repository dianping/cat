/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dianping.cat.alarm.server;

import com.dianping.cat.helper.TimeHelper;

public enum Interval {

	SECOND("s", TimeHelper.ONE_SECOND),

	MINUTE("m", TimeHelper.ONE_MINUTE),

	HOUR("h", TimeHelper.ONE_HOUR),

	DAY("d", TimeHelper.ONE_DAY),

	WEEK("w", TimeHelper.ONE_WEEK);

	private String m_name;

	private long m_time;

	private Interval(String name, long time) {
		m_name = name;
		m_time = time;
	}

	public String getName() {
		return m_name;
	}

	public long getTime() {
		return m_time;
	}

	public static Interval findByName(String name, Interval defaultValue) {
		for (Interval interval : values()) {
			if (interval.getName().equalsIgnoreCase(name)) {
				return interval;
			}
		}
		return defaultValue;
	}

	public static Interval findByInterval(String interval) {
		for (Interval intval : values()) {
			if (interval.endsWith(intval.getName())) {
				return intval;
			}
		}
		return null;
	}

}
