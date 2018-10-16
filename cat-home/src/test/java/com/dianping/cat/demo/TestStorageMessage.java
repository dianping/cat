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

import java.util.Random;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestStorageMessage {

	private String JDBC_CONNECTION = "jdbc:mysql://%s:3306/%s?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";

	@Test
	public void testCross() throws Exception {
		String serverIp = "10.10.10.1";

		while (true) {
			for (int i = 0; i < 2; i++) {
				sendCacheMsg("cache-1", "user-" + i, "get", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "remove", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "add", serverIp + i);
				sendCacheMsg("cache-1", "user-" + i, "mGet", serverIp + i);

				sendSQLMsg("sql-1", "user-" + i, "select", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "insert", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "delete", serverIp + i);
				sendSQLMsg("sql-1", "user-" + i, "update", serverIp + i);

				sendCacheMsg("cache-2", "user-" + i, "get", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "add", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "remove", serverIp + i);
				sendCacheMsg("cache-2", "user-" + i, "mGet", serverIp + i);

				sendSquirrelMsg("redis", "user-" + i, "get", serverIp + i);
				sendSquirrelMsg("redis", "user-" + i, "add", serverIp + i);
				sendSquirrelMsg("redis", "user-" + i, "remove", serverIp + i);
				sendSquirrelMsg("redis", "user-" + i, "mGet", serverIp + i);

				sendSQLMsg("sql-2", "user-" + i, "select", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "update", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "delete", serverIp + i);
				sendSQLMsg("sql-2", "user-" + i, "insert", serverIp + i);
			}
			Thread.sleep(5);
		}
	}

	private void sendCacheMsg(String name, String domain, String method, String serverIp) throws InterruptedException {
		Transaction t = Cat.newTransaction("Cache.memcached-" + name, "oUserAuthLevel:" + method);

		Cat.logEvent("Cache.memcached.server", serverIp);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		((DefaultMessageTree) tree).setDomain(domain);
		int nextInt = new Random().nextInt(1000);
		Thread.sleep(500 + nextInt);

		if (nextInt % 2 == 0) {
			t.setStatus(Transaction.SUCCESS);
		} else {
			t.setStatus("");
		}
		t.complete();
	}

	private void sendSquirrelMsg(String name, String domain, String method, String serverIp) throws InterruptedException {
		Transaction t = Cat.newTransaction("Squirrel." + name, "oUserAuthLevel:" + method);

		Cat.logEvent("Squirrel." + name + ".server", serverIp);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
		((DefaultMessageTree) tree).setDomain(domain);
		int nextInt = new Random().nextInt(1000);
		Thread.sleep(500 + nextInt);

		if (nextInt % 2 == 0) {
			t.setStatus(Transaction.SUCCESS);
		} else {
			t.setStatus("");
		}
		t.complete();
	}

	private void sendSQLMsg(String name, String domain, String method, String serverIp) throws InterruptedException {
		Transaction t = Cat.newTransaction("SQL", "sql.method");

		Cat.logEvent("SQL.Method", method);
		Cat.logEvent("SQL.Database", String.format(JDBC_CONNECTION, serverIp, name));

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(domain);
		Thread.sleep(500 + new Random().nextInt(1000));
		int nextInt = new Random().nextInt(3);

		if (nextInt % 2 == 0) {
			t.setStatus(Transaction.SUCCESS);
		} else {
			t.setStatus(String.valueOf(nextInt));
		}

		t.complete();
	}
}
