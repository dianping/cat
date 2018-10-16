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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.analysis.AbstractMessageAnalyzerTest;
import com.dianping.cat.analysis.PeriodTaskTest;
import com.dianping.cat.server.ServerConfigVisitorTest;
import com.dianping.cat.service.DefaultReportManagerTest;
import com.dianping.cat.service.ModelPeriodTest;
import com.dianping.cat.service.ModelRequestTest;
import com.dianping.cat.service.ModelResponseTest;
import com.dianping.cat.statistic.ServerStatisticManagerTest;
import com.dianping.cat.storage.message.MessageBlockTest;
import com.dianping.cat.task.TaskManagerTest;

@RunWith(Suite.class)
@SuiteClasses({

						MessageBlockTest.class,

/* .task */
						TaskManagerTest.class,

						ServerStatisticManagerTest.class,

						ModelRequestTest.class,

						ModelPeriodTest.class,

						ModelResponseTest.class,

						PeriodTaskTest.class,

						ServerConfigVisitorTest.class,

						AbstractMessageAnalyzerTest.class,

						DefaultReportManagerTest.class

})
public class AllTests {

}
