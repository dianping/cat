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

import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.transform.TransactionTrendGraphBuilder;
import com.dianping.cat.report.page.transaction.transform.TransactionTrendGraphBuilder.TransactionReportVisitor;

public class TransactionTrendGraphBuilderTest {
	@Test
	public void testVisitName() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportDailyGraph.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(xml);

		TransactionReportVisitor visitor = new TransactionTrendGraphBuilder().new TransactionReportVisitor("10.1.77.193",
								"URL", "/unreadntfcount.bin");
		visitor.visitTransactionReport(report);

		Map<String, double[]> datas = visitor.getDatas();
		assertArray(770, datas.get(TransactionTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(TransactionTrendGraphBuilder.FAIL));
		assertArray(4.7, datas.get(TransactionTrendGraphBuilder.AVG));
	}

	@Test
	public void testVisitType() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportDailyGraph.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(xml);

		TransactionReportVisitor visitor = new TransactionTrendGraphBuilder().new TransactionReportVisitor("10.1.77.193",
								"URL", "");
		visitor.visitTransactionReport(report);

		Map<String, double[]> datas = visitor.getDatas();
		assertArray(1210, datas.get(TransactionTrendGraphBuilder.COUNT));
		assertArray(0, datas.get(TransactionTrendGraphBuilder.FAIL));
		assertArray(747.5, datas.get(TransactionTrendGraphBuilder.AVG));
	}

	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
	}

}
