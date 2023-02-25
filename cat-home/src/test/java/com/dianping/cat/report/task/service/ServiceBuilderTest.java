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
package com.dianping.cat.report.task.service;

import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.service.HostinfoService;

public class ServiceBuilderTest extends ComponentTestCase {

	@Test
	public void testHourlyReport() throws Exception {
		Cat.initialize("192.168.7.43");
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildHourlyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMddHH").parse("2013082011"));
	}

	@Test
	public void testDailyReport() throws Exception {
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildDailyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130712"));
	}

	@Test
	public void testWeeklyReport() throws Exception {
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildWeeklyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130710"));
	}

	@Test
	public void testMonthlyReport() throws Exception {
		ServiceReportBuilder builder = lookup(ServiceReportBuilder.class);
		HostinfoService hostinfoService = lookup(HostinfoService.class);

		hostinfoService.initialize();
		builder.buildMonthlyTask(Constants.REPORT_SERVICE, "cat", new SimpleDateFormat("yyyyMMdd").parse("20130701"));
	}

}
