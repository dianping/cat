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
package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.core.aggregation.CompositeFormatTest;
import com.dianping.cat.consumer.core.aggregation.DefaultFormatTest;
import com.dianping.cat.consumer.cross.CrossAnalyzerTest;
import com.dianping.cat.consumer.cross.CrossInfoTest;
import com.dianping.cat.consumer.cross.CrossReportMergerTest;
import com.dianping.cat.consumer.event.EventAnalyzerTest;
import com.dianping.cat.consumer.event.EventReportMergerTest;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzerTest;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMergerTest;
import com.dianping.cat.consumer.matrix.MatrixAnalyzerTest;
import com.dianping.cat.consumer.matrix.MatrixModelTest;
import com.dianping.cat.consumer.matrix.MatrixReportMergerTest;
import com.dianping.cat.consumer.problem.ProblemAnalyzerTest;
import com.dianping.cat.consumer.problem.ProblemFilterTest;
import com.dianping.cat.consumer.problem.ProblemHandlerTest;
import com.dianping.cat.consumer.problem.ProblemReportConvertorTest;
import com.dianping.cat.consumer.problem.ProblemReportMergerTest;
import com.dianping.cat.consumer.problem.ProblemReportTest;
import com.dianping.cat.consumer.state.StateAnalyzerTest;
import com.dianping.cat.consumer.state.StateReportMergerTest;
import com.dianping.cat.consumer.top.TopAnalyzerTest;
import com.dianping.cat.consumer.top.TopReportMergerTest;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportMergerTest;
import com.dianping.cat.consumer.transaction.TransactionReportTest;

@RunWith(Suite.class)
@SuiteClasses({

						ProblemHandlerTest.class,

/* transaction */

						TransactionAnalyzerTest.class,

						TransactionReportTest.class,

						TransactionReportMergerTest.class,

/* event */
						EventAnalyzerTest.class,

						EventReportMergerTest.class,

/* heartbeat */
						HeartbeatAnalyzerTest.class,

						HeartbeatReportMergerTest.class,

/* state */
						StateAnalyzerTest.class,

						StateReportMergerTest.class,

/* top */
						TopAnalyzerTest.class,

						TopReportMergerTest.class,

/* problem */

						ProblemHandlerTest.class,

						ProblemReportTest.class,

						ProblemAnalyzerTest.class,

						ProblemReportMergerTest.class,

						CompositeFormatTest.class,

						DefaultFormatTest.class,

						ProblemFilterTest.class,

						//MetricAnalyzerTest.class,

						ProblemReportConvertorTest.class,

						CrossInfoTest.class,

						CrossReportMergerTest.class,

						MatrixModelTest.class,

						MatrixReportMergerTest.class,

						CrossAnalyzerTest.class,

						MatrixAnalyzerTest.class,	})
public class AllTests {

}
