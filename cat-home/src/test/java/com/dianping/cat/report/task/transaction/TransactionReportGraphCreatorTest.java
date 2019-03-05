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
package com.dianping.cat.report.task.transaction;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.task.TransactionReportDailyGraphCreator;
import com.dianping.cat.report.page.transaction.task.TransactionReportHourlyGraphCreator;

public class TransactionReportGraphCreatorTest {

	@Test
	public void testMergeHourlyGraph() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseTransactionReportForGraph.xml"),	"utf-8");
		TransactionReport report1 = DefaultSaxParser.parse(oldXml);
		TransactionReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO()
								.readFrom(getClass().getResourceAsStream("TransactionReportHourlyGraphResult.xml"), "utf-8");

		TransactionReport result = new TransactionReport(report1.getDomain());

		TransactionReportHourlyGraphCreator creator = new TransactionReportHourlyGraphCreator(result, 10);

		creator.createGraph(report1);
		creator.createGraph(report2);

		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(DefaultSaxParser.parse(expected),result));
		
	}

	@Test
	public void testMergeDailyGraph() throws Exception {
		String oldXml1 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyTransactionReport1.xml"),	"utf-8");
		String oldXml2 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyTransactionReport2.xml"),	"utf-8");

		TransactionReport report1 = DefaultSaxParser.parse(oldXml1);
		TransactionReport report2 = DefaultSaxParser.parse(oldXml2);
		String expected = Files.forIO()
								.readFrom(getClass().getResourceAsStream("TransactionReportDailyGraphResult.xml"), "utf-8");

		TransactionReport result = new TransactionReport(report1.getDomain());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		TransactionReportDailyGraphCreator creator = new TransactionReportDailyGraphCreator(result, 7,
								sdf.parse("2016-01-23 00:00:00"));

		creator.createGraph(report1);
		creator.createGraph(report2);
		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(DefaultSaxParser.parse(expected),result));
		

	}
}
