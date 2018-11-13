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
package com.dianping.cat.analysis;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportManager;

public class AbstractMessageAnalyzerTest extends ComponentTestCase {

	@Test
	public void testTimeOut() throws InterruptedException {
		int queueSize = 1000;
		MessageQueue queue = new DefaultMessageQueue(queueSize);
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);

		MockAnalyzer analyzer = new MockAnalyzer();
		analyzer.initialize(start, 1000, 1000);

		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(true, analyzer.isTimeout());

		int count = 2000;
		for (int i = 0; i < count; i++) {
			queue.offer(new DefaultMessageTree());
		}

		analyzer.analyze(queue);

		Assert.assertEquals(Math.min(queueSize, count), analyzer.m_count);
		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(true, analyzer.isTimeout());

		Thread.sleep(2000);
		Assert.assertEquals(true, analyzer.isTimeout());
		Assert.assertEquals(1000, analyzer.getExtraTime());
		Assert.assertEquals(start, analyzer.getStartTime());
	}

	@Test
	public void testNotTimeOut() throws InterruptedException {
		MessageQueue queue = new DefaultMessageQueue(1000);
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);

		MockAnalyzer analyzer = new MockAnalyzer();
		analyzer.initialize(start, 60 * 60 * 1000, 1000);

		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(false, analyzer.isTimeout());

		int count = 1000;
		for (int i = 0; i < count; i++) {
			queue.offer(new DefaultMessageTree());
		}
		Threads.forGroup().start(new ShutDown(analyzer));

		analyzer.analyze(queue);

		Assert.assertEquals(count, analyzer.m_count);
		Assert.assertEquals(false, analyzer.isTimeout());

		Thread.sleep(2000);
		Assert.assertEquals(false, analyzer.isTimeout());
	}

	public static class ShutDown implements Runnable {
		private MockAnalyzer m_analyzer;

		public ShutDown(MockAnalyzer analyzer) {
			m_analyzer = analyzer;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			m_analyzer.shutdown();
			Assert.assertEquals(true, m_analyzer.isActive());
		}

	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<Object> {

		public int m_count;

		@Override
		public void doCheckpoint(boolean atEnd) {
		}

		@Override
		public Object getReport(String domain) {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			m_count++;
			throw new RuntimeException("this is for test, Please ignore it");
		}

		@Override
		protected void loadReports() {
		}

		@Override
		public ReportManager<?> getReportManager() {
			return null;
		}

		@Override
		public boolean isEligable(MessageTree tree) {
			return true;
		}
	}

}
