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

import java.io.File;

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class CatEnvironmentTest {

	@Test
	public void setMuli() throws InterruptedException {
		Cat.enableMultiInstances();

		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("type1", "name");
			t.complete();
		}

		Thread.sleep(10000);
	}

	@Test
	public void testWithoutInitialize() throws InterruptedException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);
		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}

	@Test
	public void testWithInitialize() throws InterruptedException {
		Cat.initialize(new File(Cat.getCatHome(),"client.xml"));
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}

	@Test
	public void testWithNoExistGlobalConfigInitialize() throws InterruptedException {
		Cat.initialize(new File(Cat.getCatHome(),"clientNoExist.xml"));
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(100);

		Assert.assertEquals(true, Cat.isInitialized());
		Cat.destroy();
	}

	@Test
	public void testJobTest() throws Exception {
		Cat.initialize("192.168.7.70", "192.168.7.71");
		Transaction t = Cat.newTransaction("TestType", "TestName");

		t.addData("data here");
		t.setStatus("TestStatus");
		t.complete();

		Thread.sleep(10000);
	}
}
