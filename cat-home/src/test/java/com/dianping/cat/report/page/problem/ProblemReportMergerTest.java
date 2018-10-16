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
package com.dianping.cat.report.page.problem;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.task.HistoryProblemReportMerger;

public class ProblemReportMergerTest {

	@Test
	public void testProblemReportMergeAll() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeAllResult.xml"),	"utf-8");
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
								merger.getProblemReport().toString().replace("\r", ""));
		Assert.assertEquals("Source report is not changed!", newXml.replaceAll("\r", ""),
								reportNew.toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is not changed!", oldXml.replaceAll("\r", ""),
								reportOld.toString().replaceAll("\r", ""));
	}

	@Test
	public void testProblemReportMergerSize() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMobile.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		for (int i = 0; i < 24; i++) {
			reportOld.accept(merger);
		}
		ProblemReport problemReport = merger.getProblemReport();
		for (Machine machine : problemReport.getMachines().values()) {
			List<Entry> entries = machine.getEntries();
			for (Entry entry : entries) {
				int size = entry.getThreads().size();
				Assert.assertEquals(0, size);
			}
		}
		Assert.assertEquals(true, (double) problemReport.toString().length() / 1024 / 1024 < 1);
	}
}
