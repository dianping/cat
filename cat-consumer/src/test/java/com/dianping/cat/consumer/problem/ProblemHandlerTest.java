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
package com.dianping.cat.consumer.problem;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;

public class ProblemHandlerTest {
	private LongExecutionProblemHandler m_handler;

	private int[] m_defaultLongUrlDuration = { 1000, 2000, 3000, 4000, 5000 };

	private Map<String, Integer> m_longUrlThresholds = new HashMap<String, Integer>();

	@Test
	public void testHandler() {
		m_handler = new LongExecutionProblemHandler();

		for (int i = 0; i < 1000; i++) {
			Assert.assertEquals(-1, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 1000; i < 2000; i++) {
			Assert.assertEquals(1000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 2000; i < 3000; i++) {
			Assert.assertEquals(2000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 3000; i < 4000; i++) {
			Assert.assertEquals(3000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 4000; i < 5000; i++) {
			Assert.assertEquals(4000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 5000; i < 8000; i++) {
			Assert.assertEquals(5000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
	}
}
