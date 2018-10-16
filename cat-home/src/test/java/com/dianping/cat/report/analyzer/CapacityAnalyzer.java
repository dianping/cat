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
package com.dianping.cat.report.analyzer;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.task.TaskBuilder;

public class CapacityAnalyzer extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		TaskBuilder builder = lookup(TaskBuilder.class, CapacityUpdateTask.ID);

		builder.buildHourlyTask("cat", "cat", null);
		builder.buildDailyTask("cat", "cat", null);
		builder.buildWeeklyTask("cat", "cat", null);
		builder.buildMonthlyTask("cat", "cat", null);
	}

}
