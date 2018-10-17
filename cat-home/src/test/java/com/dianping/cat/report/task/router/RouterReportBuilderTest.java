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
package com.dianping.cat.report.task.router;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class RouterReportBuilderTest extends ComponentTestCase {

	@Test
	public void test() throws ParseException {
		RouterConfigBuilder builder = (RouterConfigBuilder) lookup(TaskBuilder.class, RouterConfigBuilder.ID);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-11-03 00:00:00");

		builder.buildDailyTask(Constants.REPORT_ROUTER, "cat", period);
	}

	@Test
	public void test2() throws ParseException {
		RouterConfigHandler handler = (RouterConfigHandler) lookup(RouterConfigHandler.class);
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-04-20 00:00:00");

		RouterConfig routerConfig = handler.buildRouterConfig("cat", period);

		System.out.println(routerConfig);

	}
}
