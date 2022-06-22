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
package com.dianping.cat.message.legacy;

import static com.dianping.cat.message.Message.SUCCESS;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.TraceContextHelper;

public class AppSimulator extends CatTestCase {
	@Test
	public void simulateHierarchyTransaction() throws Exception {
		Transaction t = Cat.newTransaction("URL", "WebPage");
		String id1 = TraceContextHelper.createMessageId();
		String id2 = TraceContextHelper.createMessageId();

		try {
			// do your business here
			t.addData("k1", "v1");
			t.addData("k2", "v2");
			t.addData("k3", "v3");
			Thread.sleep(5);

			Cat.logEvent("Type1", "Name1", SUCCESS, "data1");
			Cat.logEvent("Type2", "Name2", SUCCESS, "data2");
			Cat.logEvent("RemoteCall", "Service1", SUCCESS, id1);
			createChildThreadTransaction(id1, TraceContextHelper.createMessageId(), TraceContextHelper.createMessageId());
			Cat.logEvent("Type3", "Name3", SUCCESS, "data3");
			Cat.logEvent("RemoteCall", "Service1", SUCCESS, id2);
			createChildThreadTransaction(id2, TraceContextHelper.createMessageId(), TraceContextHelper.createMessageId(),
			      TraceContextHelper.createMessageId());
			Cat.logEvent("Type4", "Name4", SUCCESS, "data4");
			Cat.logEvent("Type5", "Name5", SUCCESS, "data5");
			t.setStatus(SUCCESS);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	protected void createChildThreadTransaction(final String id, final String... childIds) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				Transaction t = Cat.newTransaction("Service", "service-" + (int) (Math.random() * 10));

				// override the message id
				TraceContextHelper.threadLocal().getMessageTree().setMessageId(id);

				try {
					// do your business here
					t.addData("service data here");
					Thread.sleep(5);

					Cat.logEvent("Type1", "Name1", SUCCESS, "data1");
					Cat.logEvent("Type2", "Name2", SUCCESS, "data2");

					for (String childId : childIds) {
						Cat.logEvent("RemoteCall", "Service1", SUCCESS, childId);
						createChildThreadTransaction(childId);
					}

					Cat.logEvent("Type4", "Name4", SUCCESS, "data4");
					Cat.logEvent("Type5", "Name5", SUCCESS, "data5");
					t.setStatus(SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
				} finally {
					t.complete();
				}
			}
		};

		thread.start();

		// wait for it to complete
		try {
			thread.join();
		} catch (InterruptedException e) {
			// ignore it
		}
	}
}
