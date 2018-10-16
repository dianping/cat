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
package com.dianping.cat.report.alert.sender;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;

public class SenderManagerTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
	}

	@Test
	public void test() throws Exception {
		SenderManager manager = lookup(SenderManager.class);
		List<String> receivers = new ArrayList<String>();

		receivers.add("yong.you@dianping.com");
		SendMessageEntity message = new SendMessageEntity("Test", "test", "title", "content", receivers);
		boolean result = manager.sendAlert(AlertChannel.MAIL, message);

		System.out.println(result);
	}

}
