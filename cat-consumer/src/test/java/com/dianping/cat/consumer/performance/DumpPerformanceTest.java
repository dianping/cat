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
package com.dianping.cat.consumer.performance;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class DumpPerformanceTest extends ComponentTestCase {

	private MessageIdFactory m_factory = new MessageIdFactory();

	public void setUp() throws Exception {
		super.setUp();
		m_factory.initialize("test");
	}

	@Test
	public void test() throws Exception {
		DumpAnalyzer analyzer = (DumpAnalyzer) lookup(MessageAnalyzer.class, DumpAnalyzer.ID);

		MessageTree tree = buildMessage();

		long current = System.currentTimeMillis();

		long size = 10000000000000l;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		//cost 167 -> 77
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
										.at(1348374838231L) //
										.after(1300).child(t("QUICKIE SERVICE", "gimme_stuff", 1571)) //
										.after(100).child(e("SERVICE", "event1")) //
										.after(100).child(h("SERVICE", "heartbeat1")) //
										.after(100).child(t("WEB SERVER", "GET", 109358) //
																.after(1000).child(t("SOME SERVICE", "get", 4345) //
																						.after(4000).child(t("MEMCACHED", "Get", 279))) //
																.mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
																.reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
																						.after(1000).mark().child(t("SERVICE", "getStuff", 3760)) //
																						.reset().child(t("DATAR", "findThings", 94537)) //
																						.after(200).child(t("THINGIE", "getMoar", 1435)) //
																) //
																.after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 378)) //
																						.reset().child(t("MEMCACHED", "Get", 3496)) //
																) //
																.reset().child(t("FINAL DATA SERVICE", "get", 4394) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																).reset().child(t("123123", "get", 4394) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																).reset().child(t("123123", "get", 4394) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																).reset().child(t("123123", "get", 4394) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																) //
										) //
										;

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);

		tree.setMessageId(m_factory.getNextId());
		return tree;
	}

}
