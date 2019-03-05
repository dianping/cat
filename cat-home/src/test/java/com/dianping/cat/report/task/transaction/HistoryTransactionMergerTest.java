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

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.task.HistoryTransactionReportMerger;

public class HistoryTransactionMergerTest {

	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryTransaction.xml"), "utf-8");
		TransactionReport report1 = DefaultSaxParser.parse(oldXml);
		TransactionReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO()
								.readFrom(getClass().getResourceAsStream("HistoryTransactionMergeResult.xml"),	"utf-8");
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(report1.getDomain()))
								.setDuration(2);

		report1.accept(merger);
		report2.accept(merger);

	//	Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(DefaultSaxParser.parse(expected),merger.getTransactionReport()));

	}
}
