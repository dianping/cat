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
package com.dianping.cat.report.page.event;

import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.transform.EventTrendGraphBuilder;
import com.dianping.cat.report.page.event.transform.EventTrendGraphBuilder.EventReportVisitor;

public class EventTrendGraphBuilderTest {

	@Test
	public void testVisitName() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportDailyGraph.xml"),	"utf-8");
		EventReport report = DefaultSaxParser.parse(xml);

		EventReportVisitor visitor = new EventTrendGraphBuilder().new EventReportVisitor("10.1.77.193", "URL", "ClientInfo");
		visitor.visitEventReport(report);

		Map<String, double[]> datas = visitor.getDatas();
		assertArray(1725, datas.get(EventTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(EventTrendGraphBuilder.FAIL));
	}

	@Test
	public void testVisitType() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportDailyGraph.xml"),	"utf-8");
		EventReport report = DefaultSaxParser.parse(xml);

		EventReportVisitor visitor = new EventTrendGraphBuilder().new EventReportVisitor("10.1.77.193", "URL", "");
		visitor.visitEventReport(report);

		Map<String, double[]> datas = visitor.getDatas();
		assertArray(3450, datas.get(EventTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(EventTrendGraphBuilder.FAIL));
	}

	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
	}

}
