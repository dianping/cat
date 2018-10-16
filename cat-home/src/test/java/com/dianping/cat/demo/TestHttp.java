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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;

public class TestHttp {

	@Test
	public void testManyThread() throws Exception {
		System.setProperty("devMode", "true");
		int size = 20;
		CountDownLatch latch = new CountDownLatch(size);
		for (int i = 0; i < size; i++) {
			Threads.forGroup("cat").start(new SendEvent(i, latch));
		}

		String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());

		System.out.println(String.format("[%s] [INFO] Press any key to stop server ... ", timestamp));
		System.in.read();
	}

	public static class SendEvent implements Task {

		private int m_index;

		private CountDownLatch m_latch;

		public SendEvent(int index, CountDownLatch latch) {
			m_index = index;
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.countDown();
				m_latch.await();

				for (int i = 0; i < 1000000000; i++) {
					try {
						InputStream in = Urls.forIO().readTimeout(3000).connectTimeout(3000)
												.openStream("http://cat.qa.dianpingoa.com/cat/r/");

						String content = Files.forIO().readFrom(in, "utf-8");
						System.out.println(" id:" + m_index + " seq" + i + " length:" + content.length());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getName() {
			return "index:" + m_index;
		}

		@Override
		public void shutdown() {
		}
	}

}
