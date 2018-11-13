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
package com.dianping.cat.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestCrossMessage {

	@Test
	public void testCross() throws Exception {
		String serverIp = "10.10.10.1";
		String clientIp = "10.10.10.2";

		while (true) {
			for (int i = 0; i < 1000; i++) {
				sendClientMsg("Cat-Call", "catClient1", clientIp, "1000", "catServer1", serverIp + ":8080");
				sendClientMsg("Cat-Call", "catClient1", clientIp, "1000", "catServer2", serverIp + ":8081");
				sendClientMsg("Cat-Call", "catClient2", clientIp, "1001", "catServer1", serverIp + ":8080");
				sendClientMsg("Cat-Call", "catClient2", clientIp, "1001", "catServer2", serverIp + ":8081");
				sendServiceMsg("Cat-Call", "catServer1", serverIp, "catClient1", clientIp + ":1000");
				sendServiceMsg("Cat-Call", "catServer1", serverIp, "catClient2", clientIp + ":1001");
				sendServiceMsg("Cat-Call", "catServer2", serverIp, "catClient1", clientIp + ":1000");
				sendServiceMsg("Cat-Call", "catServer2", serverIp, "catClient2", clientIp + ":1001");
			}
		}
	}

	@Test
	public void testCross1() throws Exception {
		String serverIp = "10.10.10.1";
		String clientIp = "10.10.10.2";

		for (int i = 0; i < 1000; i++) {
			sendClientMsg("Cat-Call", "catClient1", clientIp, "1000", "catServer1", serverIp + ":1000");
			sendClientMsg("Cat-Call", "catServer1", serverIp, "1000", "catClient1", clientIp + ":1000");
			sendServiceMsg("Cat-Call", "catServer1", serverIp, "catClient1", clientIp + ":1000");
			sendServiceMsg("Cat-Call", "catClient1", clientIp, "catServer1", serverIp + ":1000");
		}
		Thread.sleep(10000);
	}

	private void sendServiceMsg(String method, String server, String serverIp, String client, String clientIp) {
		Transaction t = Cat.newTransaction("PigeonService", method);

		Cat.logEvent("PigeonService.client", clientIp);
		Cat.logEvent("PigeonService.app", client);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(server);
		((DefaultMessageTree) tree).setIpAddress(serverIp);
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	private void sendClientMsg(String method, String client, String clientIp, String port, String server,
							String serverIp) {
		Transaction t = Cat.newTransaction("PigeonCall", method);

		Cat.logEvent("PigeonCall.server", serverIp);
		Cat.logEvent("PigeonCall.app", server);
		Cat.logEvent("PigeonCall.port", port);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(client);
		((DefaultMessageTree) tree).setIpAddress(clientIp);
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	@Test
	public void test() throws InterruptedException {
		Map<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < 100; i++) {
			String key = String.valueOf(i);
			map.put(key, key);
		}
		Threads.forGroup("f").start(new TestThread(map));

		Thread.sleep(1000);

		Map<String, String> map2 = new HashMap<String, String>();
		for (int i = 100; i < 200; i++) {
			String key = String.valueOf(i);
			map2.put(key, key);
		}
		map = map2;

		Thread.sleep(10000);
	}

	public static class TestThread implements Task {

		public Map<String, String> m_map;

		public TestThread(Map<String, String> map) {
			m_map = map;
		}

		@Override
		public void run() {
			for (Entry<String, String> entry : m_map.entrySet()) {
				System.out.println(entry.getKey() + " " + entry.getValue());
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {
			// TODO Auto-generated method stub

		}

	}
}
