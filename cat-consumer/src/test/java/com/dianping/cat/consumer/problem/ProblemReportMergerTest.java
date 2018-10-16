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
package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportMergerTest {
	@Test
	public void testProblemReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeResult.xml"), "utf-8");
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Source report is not changed!", newXml.replace("\r", ""),
								reportNew.toString().replace("\r", ""));
		Assert.assertEquals("Source report is not changed!", oldXml.replace("\r", ""),
								reportOld.toString().replace("\r", ""));
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""),
								merger.getProblemReport().toString().replace("\r", ""));

	}

	@Test
	public void testMergeList() {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport("cat"));
		List<String> list1 = buildList1();
		merger.mergeList(list1, buildList2(), 10);
		Assert.assertEquals(10, list1.size());

		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 25);
		Assert.assertEquals(25, list1.size());

		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 30);
		Assert.assertEquals(30, list1.size());

		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 40);
		Assert.assertEquals(30, list1.size());
	}

	private List<String> buildList1() {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < 10; i++) {
			list.add("Str" + i);
		}
		return list;
	}

	private List<String> buildList2() {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < 20; i++) {
			list.add("Str" + i);
		}

		return list;
	}

}
