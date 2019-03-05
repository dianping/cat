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

public class TestCrashLog {

	@Test
	public void test() throws InterruptedException {
		while (true) {
			for (int i = 0; i < 10; i++) {
				Transaction t = Cat.newTransaction("AndroidCrashLog", "crashLog");

				Cat.logEvent("Error", "AndroidCrashLogTest1", "ERROR", "Crash log detail stack info A !");
				Cat.logEvent("Error", "AndroidCrashLogTest2", "ERROR", "Crash log detail stack info B !");

				String plateform = getPlateform("Android", i);
				String version = getVersion("Android", i);
				String moudle = getModule("Android", i);
				String level = getLevel("Android", i);

				MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
				((DefaultMessageTree) tree).setIpAddress(version + ":" + plateform + ":" + moudle + ":" + level);
				((DefaultMessageTree) tree).setDomain("AndroidCrashLog");
				t.complete();

				Transaction t2 = Cat.newTransaction("iOSCrashLog", "crashLog");

				Cat.logEvent("Exception", "iOSCrashLogTest1", "ERROR", "Crash log detail stack info A !");
				Cat.logEvent("Exception", "iOSCrashLogTest2", "ERROR", "Crash log detail stack info B !");

				String plateform2 = getPlateform("iOS", i);
				String version2 = getVersion("iOS", i);
				String moudle2 = getModule("iOS", i);
				String level2 = getLevel("iOS", i);

				MessageTree tree2 = Cat.getManager().getThreadLocalMessageTree();
				((DefaultMessageTree) tree2).setIpAddress(version2 + ":" + plateform2 + ":" + moudle2 + ":" + level2);
				((DefaultMessageTree) tree2).setDomain("iOSCrashLog");
				t2.complete();
			}
			Thread.sleep(10000);
			// code nullpoint
		}
	}

	private String getLevel(String platform, int index) {
		return platform + "Level" + new Random().nextInt(20);
	}

	private String getModule(String platform, int index) {
		return platform + "Module" + new Random().nextInt(20);
	}

	private String getPlateform(String platform, int index) {
		return platform + new Random().nextInt(20);
	}

	private String getVersion(String platform, int index) {
		return platform + "Version" + new Random().nextInt(20);
	}
}
