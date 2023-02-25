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
package com.dianping.cat.report.page.cross;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class CrossTest {
	@Test
	public void test() throws InterruptedException {
		while (true) {

			sendServiceMsg("CatClient-call", "cat", "10.1.2.15", "catClient", "10.1.2.16");

			sendClientMsg("Cat-Call-1", "cat", "10.1.2.15", "catServer", "10.1.2.17:3000");
			sendClientMsg("Cat-Call-2", "cat", "10.1.2.15", "catServer", "10.1.2.20:3000");
			sendClientMsg("Cat-Call-2", "cat", "10.1.2.15", "catServer", "10.1.2.20:3000");

			sendServiceMsg("Cat-Call-1", "catServer", "10.1.2.17", "cat", "10.1.2.15");
			sendServiceMsg("Cat-Call-2", "catServer", "10.1.2.20", "cat", "10.1.2.15");
			sendServiceMsg("Cat-Call-2", "catServer", "10.1.2.20", "cat", "10.1.2.15");

			sendClientMsg("CatServer-Call-1", "catServer", "10.1.2.17", "server", "10.1.2.18:3000");
			sendClientMsg("CatServer-Call-2", "catServer", "10.1.2.20", "server", "10.1.2.18:3000");
			sendClientMsg("CatServer-Call-2", "catServer", "10.1.2.20", "server", "10.1.2.18:3000");

			sendServiceMsg("Unipay-Call-1", "catServer", "10.1.2.17", "Unipay", "10.1.4.99");
			sendServiceMsg("Unipay-Call-2", "catServer", "10.1.2.20", "Unipay", "10.1.4.99");
			sendServiceMsg("Unipay-Call-2", "catServer", "10.1.2.20", "Unipay", "10.1.4.99");

			sendClientMsg("Unipay-Call-1", "Unipay", "10.1.4.99", "catServer", "10.1.2.17:3000");
			sendClientMsg("Unipay-Call-2", "Unipay", "10.1.4.99", "catServer", "10.1.2.20:3000");
			sendClientMsg("Unipay-Call-2", "Unipay", "10.1.4.99", "catServer", "10.1.2.20:3000");

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendServiceMsg(String method, String server, String serverIp, String client, String clientIp) {
		Transaction t = Cat.newTransaction("PigeonService", method);
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		((DefaultMessageTree) tree).setDomain(server);
		((DefaultMessageTree) tree).setIpAddress(serverIp);
		Cat.logEvent("PigeonService.client", clientIp);
		Cat.logEvent("PigeonService.app", client);
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	private void sendClientMsg(String method, String client, String clientIp, String server, String serverIp) {
		Transaction t = Cat.newTransaction("PigeonCall", method);
		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		((DefaultMessageTree) tree).setDomain(client);
		((DefaultMessageTree) tree).setIpAddress(clientIp);
		Cat.logEvent("PigeonCall.server", serverIp);
		Cat.logEvent("PigeonCall.app", server);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}
}
