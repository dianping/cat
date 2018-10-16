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

import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MockMessageTreeBuilder {

	private Transaction mockTransaction() { // for test/debug purpose
		return (Transaction) new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
										.at(1348374838231L) //
										.after(1300).child(t("QUICKIE SERVICE", "gimme_stuff", 1571)) //
										.after(100).child(e("SERVICE", "event1")) //
										.after(100).child(h("SERVICE", "heartbeat1")) //
										.after(100).child(t("WEB SERVER", "GET", 109358).status("1") //
																.after(1000).child(t("SOME SERVICE", "get", 4345) //
																						.after(4000).child(t("MEMCACHED", "Get", 279))) //
																.mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
																.reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
																						.after(1000).mark().child(t("SERVICE", "getStuff", 63760)) //
																						.reset().child(t("DATAR", "findThings", 94537).data("_m", "10000,30000,15000,39537")) //
																						.after(200).child(t("THINGIE", "getMoar", 1435)) //
																						.child(e("RemoteCall", "mock", "mock-message-id")) //
																) //
																.after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
																						.after(800).mark().child(t("MEMCACHED", "Get", 378)) //
																						.reset().child(t("MEMCACHED", "Get", 3496)) //
																) //
																.reset().child(t("FINAL DATA SERVICE", "get", 1902) //
																						.after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
																						.reset().child(t("MEMCACHED", "Get", 322)) //
																						.reset().child(t("MEMCACHED", "Get", 542)) //
																) //
										) //
										;

				return t;
			}
		}.build();
	}

	public MessageTree build() {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessage(mockTransaction());

		return tree;
	}
}
