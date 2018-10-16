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
package com.dianping.cat.consumer.business;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class BusinessAnalyzerTest extends ComponentTestCase {

	private final int MINITE = 60 * 1000;

	private long m_timestamp;

	private String m_domain = "group";

	private BusinessAnalyzer m_analyzer;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		m_timestamp = System.currentTimeMillis() - System.currentTimeMillis() % (3600 * 1000);
		m_analyzer = (BusinessAnalyzer) lookup(MessageAnalyzer.class, BusinessAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20160308 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 60; i++) {
			MessageTree tree = ((DefaultMessageTree) generateMessageTree(i)).copyForTest();

			m_analyzer.process(tree);
		}

		BusinessReport report = m_analyzer.getReport(m_domain);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("business_analyzer.xml"), "utf-8");

		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;

		if (i % 3 == 0) {
			t = new DefaultTransaction("URL", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + i * MINITE);
			DefaultEvent event = new DefaultEvent("URL", "ABTest");

			DefaultMetric metric = new DefaultMetric("City", "/beijing");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("S");
			metric.addData("10");

			t.addChild(metric);
			t.addChild(event);
		} else if (i % 3 == 1) {
			t = new DefaultTransaction("Service", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + i * MINITE);
			DefaultEvent event = new DefaultEvent("URL", "ABTest");

			DefaultMetric metric = new DefaultMetric("", "/nanjing");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("S,C");
			metric.addData("10,10");

			t.addChild(metric);
			t.addChild(event);
		} else {
			t = new DefaultTransaction("Metric", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + 1000);
			DefaultMetric metric = new DefaultMetric("", "/shanghai");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("C");
			metric.addData("10");

			t.addChild(metric);

			DefaultMetric durationMetric = new DefaultMetric("", "/shenzhen");

			durationMetric.setTimestamp(m_timestamp + i * MINITE);
			durationMetric.setStatus("T");
			durationMetric.addData("10");

			t.addChild(durationMetric);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		tree.setMessage(t);

		return tree;
	}
}
