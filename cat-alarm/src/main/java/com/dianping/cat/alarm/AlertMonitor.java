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

package com.dianping.cat.alarm;

/**
 * TODO
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.x
 */
public enum AlertMonitor {

	COUNT("执行次数"),
	AVG("响应时间"),
	FAILRATIO("失败率"),
	MAX("最大响应时间");

	private String text;

	AlertMonitor(String text) {
		this.text = text;
	}

	public static String parseText(String name) {
		for (AlertMonitor monitor : AlertMonitor.values()) {
			if (monitor.name().equalsIgnoreCase(name)) {
				return monitor.text;
			}
		}
		return name;
	}
}
