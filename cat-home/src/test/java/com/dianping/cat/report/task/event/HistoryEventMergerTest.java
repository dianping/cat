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

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.task.HistoryEventReportMerger;

public class HistoryEventMergerTest {
	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryBaseEvent.xml"), "utf-8");
		EventReport report1 = DefaultSaxParser.parse(oldXml);
		EventReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryEventMergerDaily.xml"), "utf-8");
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(report1.getDomain()));

		EventReport report3 = DefaultSaxParser.parse(expected);
		
		
		report1.accept(merger);
		report2.accept(merger);
	
		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(report3,merger.getEventReport()));
	}

}
