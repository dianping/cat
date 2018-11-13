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
package com.dianping.cat.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;

public class TestInternalCdn {

	@Test
	public void test() {
		List<String> keys = Arrays.asList("DiLian", "TengXun", "WangSu");
		List<String> ips = Arrays.asList("*.*.*.*", "*.*.*.*");
		Random r = new Random();

		while (true) {
			try {
				int random = r.nextInt(100);
				String key = keys.get(random % keys.size()) + ": " + ips.get(random % ips.size());
				Metric metric = Cat.getProducer().newMetric("cdn", key);
				DefaultMetric defaultMetric = (DefaultMetric) metric;

				defaultMetric.setTimestamp(System.currentTimeMillis());
				defaultMetric.setStatus("C");
				defaultMetric.addData(String.valueOf(100));

				MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
				Message message = tree.getMessage();

				if (message instanceof Transaction) {
					((DefaultTransaction) message).setTimestamp(System.currentTimeMillis());
				}
				tree.setDomain("piccenter-display");
				metric.complete();
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
