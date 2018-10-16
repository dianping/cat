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
package com.dianping.cat.report.task.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.task.TaskBuilder;

public class TransactionReportBuilderTest extends ComponentTestCase {

	@Test
	public void testDailyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildDailyTask(TransactionReportBuilder.ID, Constants.CAT,
									new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-26"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testWeeklyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildWeeklyTask(TransactionReportBuilder.ID, Constants.CAT,
									new SimpleDateFormat("yyyy-MM-dd").parse("2016-02-13"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testMonthlyTask() {
		TaskBuilder builder = lookup(TaskBuilder.class, TransactionReportBuilder.ID);

		try {
			builder.buildMonthlyTask(TransactionReportBuilder.ID, Constants.CAT,
									new SimpleDateFormat("yyyy-MM-dd").parse("2016-01-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
