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
package com.dianping.cat.report.task.service;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.service.ClientReportStatistics;

public class ClientReportStatisticsTest {

	@Test
	public void test() throws Exception {
		ClientReportStatistics statistics = new ClientReportStatistics();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("transactionReport.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(xml);
		String xml2 = Files.forIO().readFrom(getClass().getResourceAsStream("transactionReport2.xml"), "utf-8");
		TransactionReport report2 = DefaultSaxParser.parse(xml2);
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("clientReport.xml"), "utf-8");

		report.accept(statistics);
		report2.accept(statistics);
		Assert.assertEquals("Check the build result!", result.replace("\r", ""),
								statistics.getClienReport().toString().replace("\r", ""));
	}
}
