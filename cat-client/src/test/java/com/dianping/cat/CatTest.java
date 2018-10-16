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

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;

public class CatTest {

	@Test
	public void test() {
		Cat.newTransaction("logTransaction", "logTransaction");
		Cat.newEvent("logEvent", "logEvent");
		Cat.newTrace("logTrace", "logTrace");
		Cat.newHeartbeat("logHeartbeat", "logHeartbeat");
		Throwable cause = new Throwable();
		Cat.logError(cause);
		Cat.logError("message", cause);
		Cat.logTrace("logTrace", "<trace>");
		Cat.logTrace("logTrace", "<trace>", Trace.SUCCESS, "data");
		Cat.logMetric("logMetric", "test", "test");
		Cat.logMetricForCount("logMetricForCount");
		Cat.logMetricForCount("logMetricForCount", 4);
		Cat.logMetricForDuration("logMetricForDuration", 100);
		Cat.logMetricForSum("logMetricForSum", 100);
		Cat.logMetricForSum("logMetricForSum", 100, 100);
		Cat.logEvent("RemoteLink", "Call", Message.SUCCESS, "Cat-0a010680-384736-2061");
		Cat.logEvent("EventType", "EventName");
		Cat.logHeartbeat("logHeartbeat", "logHeartbeat", Message.SUCCESS, null);

		Assert.assertEquals(true, Cat.isInitialized());
	}
}
