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

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.cat.message.Message;

public class MockMessageBuilderTest {
	@Test
	public void test() {
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
																) //
										) //
										;

				return t;
			}
		}.build();

		Assert.assertEquals("t2012-09-23 12:33:58.231	WEB CLUSTER	GET	\n" + //
								"A2012-09-23 12:33:58.232	QUICKIE SERVICE	gimme_stuff	0	1571us		\n" + //
								"E2012-09-23 12:33:58.233	SERVICE	event1	0		\n" + //
								"H2012-09-23 12:33:58.234	SERVICE	heartbeat1	0		\n" + //
								"t2012-09-23 12:33:58.234	WEB SERVER	GET	\n" + //
								"t2012-09-23 12:33:58.235	SOME SERVICE	get	\n" + //
								"A2012-09-23 12:33:58.239	MEMCACHED	Get	0	279us		\n" + //
								"T2012-09-23 12:33:58.239	SOME SERVICE	get	0	4345us		\n" + //
								"A2012-09-23 12:33:58.239	MEMCACHED	Inc	0	319us		\n" + //
								"t2012-09-23 12:33:58.240	BIG ASS SERVICE	getThemDatar	\n" + //
								"A2012-09-23 12:33:58.241	SERVICE	getStuff	0	3760us		\n" + //
								"A2012-09-23 12:33:58.241	DATAR	findThings	0	94537us		\n" + //
								"A2012-09-23 12:33:58.335	THINGIE	getMoar	0	1435us		\n" + //
								"T2012-09-23 12:33:58.337	BIG ASS SERVICE	getThemDatar	0	97155us		\n" + //
								"t2012-09-23 12:33:58.337	OTHER DATA SERVICE	get	\n" + //
								"A2012-09-23 12:33:58.338	MEMCACHED	Get	0	378us		\n" + //
								"A2012-09-23 12:33:58.338	MEMCACHED	Get	0	3496us		\n" + //
								"T2012-09-23 12:33:58.341	OTHER DATA SERVICE	get	0	4394us		\n" + //
								"t2012-09-23 12:33:58.337	FINAL DATA SERVICE	get	\n" + //
								"A2012-09-23 12:33:58.338	MEMCACHED	Get	0	386us		\n" + //
								"A2012-09-23 12:33:58.338	MEMCACHED	Get	0	322us		\n" + //
								"A2012-09-23 12:33:58.338	MEMCACHED	Get	0	322us		\n" + //
								"T2012-09-23 12:33:58.341	FINAL DATA SERVICE	get	0	4394us		\n" + //
								"T2012-09-23 12:33:58.343	WEB SERVER	GET	0	109358us		\n" + //
								"T2012-09-23 12:33:58.343	WEB CLUSTER	GET	0	112819us		\n" + //
								"", message.toString().replace("\r", ""));
	}
}
