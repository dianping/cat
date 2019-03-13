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
public class EventTest {
	@Test
	public void testNormal() {
		Event event = Cat.getProducer().newEvent("Review", "New");

		event.addData("id", 12345);
		event.addData("user", "john");
		event.setStatus(Message.SUCCESS);
		event.complete();
	}

	@Test
	public void testException() {
		Cat.getProducer().logError(new RuntimeException());
	}

	@Test
	public void testInOneShot() {
		// Normal case
		Cat.getProducer().logEvent("Review", "New", Message.SUCCESS, "id=12345&user=john");

		// Exception case
		Cat.getProducer().logError(new RuntimeException());
	}
}
