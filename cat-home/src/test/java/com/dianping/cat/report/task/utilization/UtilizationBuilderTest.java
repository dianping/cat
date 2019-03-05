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
package com.dianping.cat.report.task.utilization;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.service.HostinfoService;

public class UtilizationBuilderTest extends ComponentTestCase {

	@Test
	public void testHourlyReport() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildHourlyTask(Constants.REPORT_UTILIZATION, Constants.CAT,
								new SimpleDateFormat("yyyyMMddHH").parse("2013082617"));
	}

	@Test
	public void testDailyReport() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildDailyTask(Constants.REPORT_UTILIZATION, Constants.CAT,
								new SimpleDateFormat("yyyyMMdd").parse("20130826"));
	}

	@Test
	public void testWeeklyReport() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildWeeklyTask(Constants.REPORT_UTILIZATION, Constants.CAT,
								new SimpleDateFormat("yyyyMMdd").parse("20130717"));
	}

	@Test
	public void testMonthlyReport() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildMonthlyTask(Constants.REPORT_UTILIZATION, Constants.CAT,
								new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

}
