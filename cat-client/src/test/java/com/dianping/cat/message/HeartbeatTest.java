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
package com.dianping.cat.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class HeartbeatTest {
	@Test
	public void testInOneShot() {
		Cat.getProducer().logHeartbeat("System", "Status", "0",
								"ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M");
	}

	@Test
	public void testService() {
		Heartbeat heartbeat = Cat.getProducer().newHeartbeat("Service", "ReviewService");

		heartbeat.addData("host", "192.168.10.112:1234");
		heartbeat.addData("weight", "20");
		heartbeat.addData("visits", "12345");
		heartbeat.addData("manifest", "addReview,getReview,getShopReviews");
		heartbeat.addData("more", "...");
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();
	}

	@Test
	public void testStatus() {
		Heartbeat heartbeat = Cat.getProducer().newHeartbeat("System", "Status");

		heartbeat.addData("ip", "192.168.10.111");
		heartbeat.addData("host", "host-1");
		heartbeat.addData("load", "2.1");
		heartbeat.addData("cpu", "0.12,0.10");
		heartbeat.addData("memory.total", "2G");
		heartbeat.addData("memory.free", "456M");
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();
	}
}
