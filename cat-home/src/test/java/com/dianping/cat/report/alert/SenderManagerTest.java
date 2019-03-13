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
package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;

public class SenderManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		SenderManager manager = lookup(SenderManager.class);

		List<String> receivers = new ArrayList<String>();
		SendMessageEntity message = new SendMessageEntity("group", "title11", "type", "content22", receivers);

		receivers.add("yong.you@dianping.com");
		receivers.add("yong.you2@dianping.com");
		manager.sendAlert(AlertChannel.MAIL, message);

		receivers.clear();
		receivers.add("18616671676");
		manager.sendAlert(AlertChannel.SMS, message);
	}

}
