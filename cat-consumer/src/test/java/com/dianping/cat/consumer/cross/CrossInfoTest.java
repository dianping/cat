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
package com.dianping.cat.consumer.cross;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer.CrossInfo;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class CrossInfoTest extends ComponentTestCase {
	public MessageTree buildMockMessageTree() {
		MessageTree tree = new DefaultMessageTree();
		tree.setMessageId("Cat-c0a80746-373452-6");// 192.168.7.70 machine logview
		tree.setIpAddress("192.168.0.1");
		return tree;
	}

	@Test
	public void testParseOtherTransaction() throws Exception {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		analyzer.setServerConfigManager(lookup(ServerConfigManager.class));
		analyzer.setIpConvertManager(new IpConvertManager());

		DefaultTransaction t = new DefaultTransaction("Other", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(true, info == null);
	}

	@Test
	public void testParsePigeonClientTransaction() throws Exception {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		analyzer.setServerConfigManager(lookup(ServerConfigManager.class));
		analyzer.setIpConvertManager(new IpConvertManager());

		DefaultTransaction t = new DefaultTransaction("PigeonCall", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), null);

		Message message = new DefaultEvent("PigeonCall.server", "10.1.1.1", null);
		Message messageApp = new DefaultEvent("PigeonCall.app", "myDomain", null);
		t.addChild(message);
		t.addChild(messageApp);

		info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "10.1.1.1");
		Assert.assertEquals(info.getDetailType(), "PigeonCall");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Server");
		Assert.assertEquals(info.getApp(), "myDomain");
	}

	@Test
	public void testParsePigeonServerTransaction() throws Exception {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		analyzer.setServerConfigManager(lookup(ServerConfigManager.class));
		analyzer.setIpConvertManager(new IpConvertManager());

		DefaultTransaction t = new DefaultTransaction("PigeonService", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(info.validate(), false);

		Message message = new DefaultEvent("PigeonService.client", "192.168.7.71", null);
		Message messageApp = new DefaultEvent("PigeonService.app", "myDomain", null);
		t.addChild(message);
		t.addChild(messageApp);

		info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "192.168.7.71");
		Assert.assertEquals(info.getDetailType(), "PigeonService");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Client");
		Assert.assertEquals(info.getApp(), "myDomain");
	}

	@Test
	public void testParsePigeonServerTransactionWithPort() throws Exception {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		analyzer.setServerConfigManager(lookup(ServerConfigManager.class));
		analyzer.setIpConvertManager(new IpConvertManager());

		DefaultTransaction t = new DefaultTransaction("PigeonService", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCrossTransaction(t, tree);

		Message message = new DefaultEvent("PigeonService.client", "192.168.7.71:29987", null);
		Message messageApp = new DefaultEvent("PigeonService.app", "myDomain", null);
		t.addChild(message);
		t.addChild(messageApp);

		info = analyzer.parseCrossTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "192.168.7.71:29987");
		Assert.assertEquals(info.getDetailType(), "PigeonService");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Client");
		Assert.assertEquals(info.getApp(), "myDomain");
	}
}
