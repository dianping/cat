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
package com.dianping.cat.consumer.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.TestHelper;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionReportMergerTest {
	@Test
	public void testTransactionReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_new.xml"), "utf-8");
		TransactionReport reportOld = DefaultSaxParser.parse(oldXml);
		TransactionReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO()
								.readFrom(getClass().getResourceAsStream("transaction_report_mergeResult.xml"),	"utf-8");
		
		TransactionReport reportExpected = DefaultSaxParser.parse(expected);
		
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertTrue("Check the merge result!",TestHelper.isEquals(reportExpected,merger.getTransactionReport()));
		
		//Assert.assertTrue("Source report is changed!", isEquals(newXml, reportNew.toString()));
	}
	
}
