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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;

public class RpcLogviewTest {

	@Before
	public void setUp() {
		new File(Cat.getCatHome(),"cat-cat.mark").delete();
	}

	@Test
	public void testClientMessage() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("test", "test");
			final Map<String, String> map = new HashMap<String, String>();
			Context ctx = new Context() {

				@Override
				public String getProperty(String key) {
					return map.get(key);
				}

				@Override
				public void addProperty(String key, String value) {
					map.put(key, value);
				}
			};
			Cat.logRemoteCallClient(ctx);

			System.out.println(Cat.getManager().getThreadLocalMessageTree());
			t.complete();
		}

		Thread.sleep(1000);
	}

	@Test
	public void testServerMessage() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("test", "test");
			final Map<String, String> map = new HashMap<String, String>();
			final String msgId = Cat.getCurrentMessageId();
			Context ctx = new Context() {

				@Override
				public String getProperty(String key) {
					return msgId;
				}

				@Override
				public void addProperty(String key, String value) {
					map.put(key, value);
				}
			};
			Cat.logRemoteCallServer(ctx);

			System.out.println(Cat.getManager().getThreadLocalMessageTree());
			t.complete();
		}

		Thread.sleep(1000);
	}

}
