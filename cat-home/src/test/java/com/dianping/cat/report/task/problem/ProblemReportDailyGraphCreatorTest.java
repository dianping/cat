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
package com.dianping.cat.report.task.problem;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.problem.task.ProblemReportDailyGraphCreator;

public class ProblemReportDailyGraphCreatorTest {

	@Test
	public void test() throws Exception {
		String oldXml1 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyProblemReport1.xml"),	"utf-8");
		String oldXml2 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyProblemReport2.xml"),	"utf-8");

		ProblemReport report1 = DefaultSaxParser.parse(oldXml1);
		ProblemReport report2 = DefaultSaxParser.parse(oldXml2);
		String expected = Files.forIO()
								.readFrom(getClass().getResourceAsStream("ProblemReportDailyGraphResult.xml"), "utf-8");

		ProblemReport result = new ProblemReport(report1.getDomain());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ProblemReportDailyGraphCreator creator = new ProblemReportDailyGraphCreator(result, 7,
								sdf.parse("2016-01-23 00:00:00"));

		creator.createGraph(report1);
		creator.createGraph(report2);

		String actual = new DefaultXmlBuilder().buildXml(result);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
