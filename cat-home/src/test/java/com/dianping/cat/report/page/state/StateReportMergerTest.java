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
package com.dianping.cat.report.page.state;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.state.task.HistoryStateReportMerger;

public class StateReportMergerTest {

	@Test
	public void testHistoryStateReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		StateReport reportOld = DefaultSaxParser.parse(oldXml);
		StateReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("historyResult.xml"), "utf-8");

		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
								merger.getStateReport().toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\r", ""),
								reportNew.toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\r", ""),
								reportOld.toString().replaceAll("\r", ""));
	}

}
