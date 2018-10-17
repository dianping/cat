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
package com.dianping.cat.consumer.dump;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TreeHelper {

	public static Message m_message = new MockMessageBuilder() {
		@Override
		public MessageHolder define() {
			TransactionHolder t = t("WEBCLUSTER", "GET", "This&123123&1231&3&\n\n\n\n&\t\t\t\n\n\n\n\n\n is test data\t\t\n\n",
									112819) //
									.at(1455333904000L) //
									.after(1300).child(t("QUICKIESERVICE", "gimme_stuff", 1571)) //
									.after(100).child(e("SERVICE", "event1", "This\n\n\n\n\n\n is test data\t\t\n\n")) //
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

	private static byte[] m_data;

	public static void init(MessageCodec codec) {
		MessageId id = MessageId.parse("domain0-0a010200-405746-0");
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain(id.getDomain());
		tree.setHostName("mock-host");
		tree.setIpAddress(id.getIpAddress());
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessageId(id.toString());
		// test for rpc index
		tree.setSessionToken(id.toString());

		if (codec != null) {
			ByteBuf buf = codec.encode(tree);
			tree.setBuffer(buf);

			m_data = buf.array();
		}
	}

	public static DefaultMessageTree tree(MessageCodec codec, MessageId id) {

		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain(id.getDomain());
		tree.setHostName("mock-host");
		tree.setIpAddress(id.getIpAddress());
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(m_message);
		tree.setMessageId(id.toString());
		// test for rpc index
		tree.setSessionToken(id.toString());

		if (codec != null) {
			ByteBuf buf = codec.encode(tree);
			tree.setBuffer(buf);
		}

		return tree;
	}

	public static DefaultMessageTree cacheTree(MessageCodec codec, MessageId id) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain(id.getDomain());
		tree.setHostName("mock-host");
		tree.setIpAddress(id.getIpAddress());
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(m_message);
		tree.setMessageId(id.toString());
		// test for rpc index
		tree.setSessionToken(id.toString());

		if (codec != null) {
			ByteBuf buf = Unpooled.copiedBuffer(m_data);

			tree.setBuffer(buf);
		}

		return tree;
	}

	public static DefaultMessageTree tree(MessageCodec codec, String id) {
		return tree(codec, MessageId.parse(id));
	}

	public static DefaultMessageTree tree(MessageId id) {
		return tree(null, id);
	}
}