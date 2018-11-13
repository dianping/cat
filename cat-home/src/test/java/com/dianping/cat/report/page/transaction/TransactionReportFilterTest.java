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
package com.dianping.cat.report.page.transaction;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.TestHelper;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService.TransactionReportFilter;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);
		TransactionReportFilter f1 = new TransactionReportFilter(null, null, "10.1.77.193", 0, 59);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter_type.xml"), "utf-8");
		TransactionReport report4expected1 = DefaultSaxParser.parse(expected1);
		String input = f1.buildXml(report);
		TestHelper.assertEquals(report4expected1, input);

		TransactionReportFilter f2 = new TransactionReportFilter("URL", null, null, 0, 59);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter_name.xml"), "utf-8");
		
		String input2 = f2.buildXml(report);
		TestHelper.assertEquals(DefaultSaxParser.parse(expected2), input2);

//		Assert.assertEquals(expected2.replaceAll("\r", ""), f2.buildXml(report).replaceAll("\r", ""));
		
	}
}
