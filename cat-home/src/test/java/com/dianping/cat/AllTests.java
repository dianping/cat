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
package com.dianping.cat;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.alert.ExtractDataTest;
import com.dianping.cat.report.alert.JudgeTimeTest;
import com.dianping.cat.report.alert.MetricIdAndRuleMappingTest;
import com.dianping.cat.report.alert.RuleConfigTest;
import com.dianping.cat.report.graph.ValueTranslaterTest;
import com.dianping.cat.report.page.cross.CrossReportMergerTest;
import com.dianping.cat.report.page.event.EventReportFilterTest;
import com.dianping.cat.report.page.event.EventTrendGraphBuilderTest;
import com.dianping.cat.report.page.problem.ProblemReportMergerTest;
import com.dianping.cat.report.page.problem.ProblemTrendGraphBuilderTest;
import com.dianping.cat.report.page.state.StateReportMergerTest;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionReportFilterTest;
import com.dianping.cat.report.page.transaction.TransactionTrendGraphBuilderTest;
import com.dianping.cat.report.task.TaskConsumerTest;
import com.dianping.cat.report.task.TaskHelperTest;
import com.dianping.cat.report.task.event.EventGraphCreatorTest;
import com.dianping.cat.report.task.event.HistoryEventMergerTest;
import com.dianping.cat.report.task.heartbeat.HeartbeatDailyMergerTest;
import com.dianping.cat.report.task.heavy.HeavyReportBuilderTest;
import com.dianping.cat.report.task.metric.AlertConfigTest;
import com.dianping.cat.report.task.problem.ProblemReportDailyGraphCreatorTest;
import com.dianping.cat.report.task.problem.ProblemReportHourlyGraphCreatorTest;
import com.dianping.cat.report.task.service.ServiceReportMergerTest;
import com.dianping.cat.report.task.storage.HistoryStorageReportMergerTest;
import com.dianping.cat.report.task.transaction.HistoryTransactionMergerTest;
import com.dianping.cat.report.task.transaction.TransactionReportGraphCreatorTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.graph */
						ValueTranslaterTest.class,

/* .report.page.model */
						EventReportFilterTest.class,

						TransactionReportFilterTest.class,

						ProblemReportMergerTest.class,

/* . report.page.transcation */
						PayloadTest.class,

/* . report.page.cross */
						CrossReportMergerTest.class,

/* .report.task */
						TaskConsumerTest.class,

						TaskHelperTest.class,

						HistoryEventMergerTest.class,

						HistoryTransactionMergerTest.class,

						ProblemReportHourlyGraphCreatorTest.class,

						ProblemReportDailyGraphCreatorTest.class,

						TransactionReportGraphCreatorTest.class,

						EventGraphCreatorTest.class,

						StateReportMergerTest.class,

/* Graph */
						EventTrendGraphBuilderTest.class,

						ProblemTrendGraphBuilderTest.class,

						TransactionTrendGraphBuilderTest.class,

/* service */
						ServiceReportMergerTest.class,

						HistoryStorageReportMergerTest.class,

						AlertConfigTest.class,

						HeavyReportBuilderTest.class,

						RuleConfigTest.class,

						AlertConfigTest.class,

						HeartbeatDailyMergerTest.class,

						MetricIdAndRuleMappingTest.class,

						ExtractDataTest.class,

						JudgeTimeTest.class })
public class AllTests {

	@BeforeClass
	public static void setUp() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Locale.setDefault(Locale.CHINESE);
	}
}
