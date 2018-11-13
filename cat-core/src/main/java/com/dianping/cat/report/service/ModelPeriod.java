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
package com.dianping.cat.report.service;

import com.dianping.cat.Constants;

public enum ModelPeriod {
	CURRENT,
	HISTORICAL,
	LAST;

	public static ModelPeriod getByName(String name, ModelPeriod defaultValue) {
		for (ModelPeriod period : values()) {
			if (period.name().equals(name)) {
				return period;
			}
		}

		return defaultValue;
	}

	public static ModelPeriod getByTime(long timestamp) {
		long current = System.currentTimeMillis();

		current -= current % Constants.HOUR;

		if (timestamp >= current) {
			return ModelPeriod.CURRENT;
		} else if (timestamp >= current - Constants.HOUR) {
			return ModelPeriod.LAST;
		} else {
			return ModelPeriod.HISTORICAL;
		}
	}

	public long getStartTime() {
		long current = System.currentTimeMillis();

		current -= current % Constants.HOUR;

		switch (this) {
		case CURRENT:
			return current;
		case LAST:
			return current - Constants.HOUR;
		default:
			return current;
		}
	}

	public boolean isCurrent() {
		return this == CURRENT;
	}

	public boolean isHistorical() {
		return this == HISTORICAL;
	}

	public boolean isLast() {
		return this == LAST;
	}
}