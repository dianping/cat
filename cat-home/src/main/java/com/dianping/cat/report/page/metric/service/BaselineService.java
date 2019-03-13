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
package com.dianping.cat.report.page.metric.service;

import java.util.Date;

import com.dianping.cat.home.dal.report.Baseline;

public interface BaselineService {

	public void insertBaseline(Baseline baseline);

	public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod);

	public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod);

	public boolean hasDailyBaseline(String reportName, String key, Date reportPeriod);

	public double[] queryBaseline(int currentMinute, int ruleMinute, String metricKey, String name);

}
