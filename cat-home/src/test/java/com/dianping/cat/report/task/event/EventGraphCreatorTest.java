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
package com.dianping.cat.report.task.event;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.event.task.EventReportDailyGraphCreator;
import com.dianping.cat.report.page.event.task.EventReportHourlyGraphCreator;

public class EventGraphCreatorTest {

	@Test
	public void testMergeHourlyGraph() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseEventGraphReport.xml"),	"utf-8");
		EventReport report1 = DefaultSaxParser.parse(oldXml);
		EventReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportHourlyGraphResult.xml"), "utf-8");

		EventReport result = new EventReport(report1.getDomain());

		EventReportHourlyGraphCreator creator = new EventReportHourlyGraphCreator(result, 10);

		creator.createGraph(report1);
		creator.createGraph(report2);

		String actual = new DefaultXmlBuilder().buildXml(result);
		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(DefaultSaxParser.parse(expected),DefaultSaxParser.parse(actual)));
		
	}

	@Test
	public void testMergeDailyGraph() throws Exception {
		String oldXml1 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyEventReport1.xml"),	"utf-8");
		String oldXml2 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyEventReport2.xml"),	"utf-8");

		EventReport report1 = DefaultSaxParser.parse(oldXml1);
		EventReport report2 = DefaultSaxParser.parse(oldXml2);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportDailyGraphResult.xml"), "utf-8");

		EventReport result = new EventReport(report1.getDomain());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		EventReportDailyGraphCreator creator = new EventReportDailyGraphCreator(result, 7, sdf.parse("2016-01-23 00:00:00"));

		creator.createGraph(report1);
		creator.createGraph(report2);

		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(DefaultSaxParser.parse(expected),result));
		
	}
}
