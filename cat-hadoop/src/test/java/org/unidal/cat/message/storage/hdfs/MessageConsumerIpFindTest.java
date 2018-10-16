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
package org.unidal.cat.message.storage.hdfs;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.internal.MessageId;

public class MessageConsumerIpFindTest extends ComponentTestCase {

	@Before
	public void before() throws Exception {
		ServerConfigManager config = lookup(ServerConfigManager.class);

		config.initialize(new File(MessageConsumerIpFindTest.class.getClassLoader().getResource("server.xml").getFile()));
	}

	@Test
	public void test() {
		MessageConsumerFinder find = lookup(MessageConsumerFinder.class, "hdfs");
		MessageId id = MessageId.parse("shop-web-0a420d56-405915-16");
		Set<String> ips = find.findConsumerIps(id.getDomain(), id.getHour());

		System.err.println(ips);

	}
}
