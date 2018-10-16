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
package com.dianping.cat.report.page.metric.task;

import java.util.Arrays;

import org.unidal.lookup.annotation.Named;

@Named
public class BaselineConfigManager {

	public BaselineConfig queryBaseLineConfig(String key) {
		BaselineConfig config = new BaselineConfig();
		Integer[] days = { -21, -14, -7, 0 };
		Double[] weights = { 1.0, 2.0, 3.0, 4.0 };

		config.setDays(Arrays.asList(days));
		config.setId(1);
		config.setKey(key);
		config.setTargetDate(7);
		config.setWeights(Arrays.asList(weights));
		return config;
	}
}
