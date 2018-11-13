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
package com.dianping.cat.report.task.cached;

import java.io.File;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.current.CurrentReportBuilder;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask;
import com.dianping.cat.report.task.current.CurrentWeeklyMonthlyReportTask.CurrentWeeklyMonthlyTask;

public class CachedReportBuilerTest extends ComponentTestCase {

	private int m_index = 0;

	@Test
	public void test() throws Exception {
		ServerConfigManager manager = (ServerConfigManager) lookup(ServerConfigManager.class);

		manager.initialize(new File(Cat.getCatHome(),"server.xml"));

		TaskBuilder builder = lookup(TaskBuilder.class, CurrentReportBuilder.ID);
		CurrentWeeklyMonthlyReportTask.getInstance().register(new CurrentWeeklyMonthlyTask() {

			@Override
			public String getReportName() {
				return "Test";
			}

			@Override
			public void buildCurrentWeeklyTask(String name, String domain, Date start) {
				m_index++;
			}

			@Override
			public void buildCurrentMonthlyTask(String name, String domain, Date start) {
				m_index++;
			}
		});

		builder.buildDailyTask("test", "test", TimeHelper.getCurrentDay());

		Thread.sleep(1000 * 5);

		Assert.assertEquals(true, m_index > 0);
	}

}