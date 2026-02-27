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

import com.dianping.cat.component.CatComponentFactoryTest;
import com.dianping.cat.component.ComponentContextTest;
import com.dianping.cat.component.ComponentLifecycleTest;
import com.dianping.cat.component.LoggerTest;
import com.dianping.cat.component.ServiceLoaderComponentFactoryTest;
import com.dianping.cat.configuration.ConfigureManagerTest;
import com.dianping.cat.configuration.ConfigureModelTest;
import com.dianping.cat.message.MessageTest;
import com.dianping.cat.message.MetricTest;
import com.dianping.cat.message.context.MessageContextTest;
import com.dianping.cat.message.context.MessageIdFactoryTest;
import com.dianping.cat.message.internal.MockMessageBuilderTest;
import com.dianping.cat.message.pipeline.MessagePipelineTest;
import com.dianping.cat.message.pipeline.MetricAggregatorTest;
import com.dianping.cat.status.StatusModelTest;
import com.dianping.cat.support.SplittersTest;
import com.dianping.cat.support.servlet.CatFilterTest;

@RunWith(Suite.class)
@SuiteClasses({

      CatBootstrapTest.class,

      /* .component */
      CatComponentFactoryTest.class,

      ComponentContextTest.class,

      ComponentLifecycleTest.class,

      LoggerTest.class,

      ServiceLoaderComponentFactoryTest.class,

      /* .configuration */
      ConfigureManagerTest.class,

      ConfigureModelTest.class,

      /* .message */
      MessageTest.class,

      MetricTest.class,

      MessageContextTest.class,

      MessageIdFactoryTest.class,

      /* .internal */
      MockMessageBuilderTest.class,

      MetricAggregatorTest.class,

      /* pipeline */
      MessagePipelineTest.class,

      StatusModelTest.class,

      /* .servlet */
      CatFilterTest.class,

      /* .tool */
      SplittersTest.class,

// MultiThreadingTest.class

})
public class AllTests {

}
