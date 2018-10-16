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
package com.dianping.cat.opensource;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

public class OpensourceTest {

	@Test
	public void testTransaction() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("JavaClient7", "Bucket_" + String.valueOf(i % 10));

			try {
				Thread.sleep(5);
				t.setDurationInMillis(calMills(i));
				t.setSuccessStatus();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				t.complete();
			}
		}
		Thread.sleep(10 * 1000L);
	}

	private long calMills(int i) {
		long mills = i % 10 * 100L;

		if (i >= 950) {
			mills += 10;
		}
		return mills;
	}

	@Test
	public void testEvent() throws Exception {
		for (int i = 0; i < 1000; i++) {
			Cat.logEvent("JavaClient1", "Bucket_" + String.valueOf(i % 10));
			Thread.sleep(5);
		}
		//t.complete();
		Thread.sleep(10 * 1000L);
	}

}
