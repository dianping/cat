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
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportManager;

public class PeriodTaskTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		int size = 100;
		MessageQueue queue = new DefaultMessageQueue(size);
		MockAnalyzer analyzer = new MockAnalyzer();
		String domain = "cat";

		analyzer.initialize(start, 1000, 1000);

		PeriodTask task = new PeriodTask(analyzer, queue, start);

		for (int i = 0; i < 110; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();

			tree.setDomain(domain);
			task.enqueue(tree);
		}
		task.run();

		Assert.assertEquals(size, analyzer.m_count);
		Assert.assertEquals(analyzer, task.getAnalyzer());
		Assert.assertEquals(true, task.getName().startsWith("MockAnalyzer"));
		task.shutdown();
	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<Object> {

		public int m_count;

		public int getCount() {
			return m_count;
		}

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
			if (m_count % 10 == 0) {
				throw new RuntimeException("this is for test, Please ignore it");
			}
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
