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
package com.dianping.cat.report;

import java.util.Map;
import java.util.Set;

import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public interface ReportManager<T> {

	public void destory();

	public void initialize();

	public Set<String> getDomains(long startTime);

	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist);

	public Map<String, T> getHourlyReports(long startTime);

	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy, int index);

	public Map<String, T> loadLocalReports(long startTime, int index);

	public void storeHourlyReports(long startTime, StoragePolicy policy, int index);

}
