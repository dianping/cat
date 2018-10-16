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

import java.util.Date;
import java.util.Set;

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;

public interface ReportService<T> {
	public boolean insertDailyReport(DailyReport report, byte[] content);

	public boolean insertHourlyReport(HourlyReport report, byte[] content);

	public boolean insertMonthlyReport(MonthlyReport report, byte[] content);

	public boolean insertWeeklyReport(WeeklyReport report, byte[] content);

	public Set<String> queryAllDomainNames(Date start, Date end, String name);

	public T queryDailyReport(String domain, Date start, Date end);

	public T queryHourlyReport(String domain, Date start, Date end);

	public T queryMonthlyReport(String domain, Date start);

	public T queryWeeklyReport(String domain, Date start);

	public T queryReport(String domain, Date start, Date end);

}