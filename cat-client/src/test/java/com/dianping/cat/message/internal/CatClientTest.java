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
package com.dianping.cat.message.internal;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.configuration.model.entity.Domain;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageQueue;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.legacy.CatTestCase;
import com.dianping.cat.message.tree.MessageTree;
import com.dianping.cat.util.Files;
import com.dianping.cat.util.Reflects;

public class CatClientTest extends CatTestCase {
	private Queue<MessageTree> m_queue;

	@BeforeClass
	public static void beforeClass() throws IOException {
		ClientConfig clientConfig = new ClientConfig();

		clientConfig.setDomain(new Domain().setName("Test"));

		File configFile = new File(Cat.getCatHome(), "client.xml").getCanonicalFile();

		configFile.getParentFile().mkdirs();

		Files.forIO().writeTo(configFile, clientConfig.toString());

		// Cat.destroy();
		Cat.getBootstrap().initialize(configFile);
	}

	@Before
	public void before() throws Exception {
		TransportManager manager = lookup(TransportManager.class);
		MessageQueue queue = Reflects.forField().getDeclaredFieldValue(manager.getSender().getClass(), "m_queue",
		      manager.getSender());

		m_queue = Reflects.forField().getDeclaredFieldValue(queue.getClass(), "m_queue", queue);
	}

	public void testNormal() throws Exception {
		Transaction t = Cat.newTransaction("URL", "MyPage");

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");

			Thread.sleep(20);

			Cat.logEvent("URL", "Payload", Message.SUCCESS, "host=my-host&ip=127.0.0.1&agent=...");
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}

		// please stop CAT server when you run this test case
		Assert.assertEquals("One message should be in the queue.", 1, m_queue.size());

		MessageTree tree = m_queue.poll();
		Message m = tree.getMessage();

		Assert.assertTrue(Transaction.class.isAssignableFrom(m.getClass()));

		Transaction trans = (Transaction) m;

		Assert.assertEquals("URL", trans.getType());
		Assert.assertEquals("MyPage", trans.getName());
		Assert.assertEquals("0", trans.getStatus());
		Assert.assertTrue(trans.getDurationInMillis() > 0);
		Assert.assertEquals("k1=v1&k2=v2&k3=v3", trans.getData().toString());

		Assert.assertEquals(1, trans.getChildren().size());

		Message c = trans.getChildren().get(0);

		Assert.assertEquals("URL", c.getType());
		Assert.assertEquals("Payload", c.getName());
		Assert.assertEquals("0", c.getStatus());
		Assert.assertEquals("host=my-host&ip=127.0.0.1&agent=...", c.getData().toString());
	}

}
