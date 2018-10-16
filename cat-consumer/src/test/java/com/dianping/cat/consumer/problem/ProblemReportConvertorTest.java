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

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportConvertorTest {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problemReportForConvert.xml"), "utf-8");
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("problemReportConvertResult.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(oldXml);
		ProblemReportConvertor convertor = new ProblemReportConvertor();
		report.accept(convertor);
		Assert.assertEquals("Source report is changed!", result.replace("\r", ""), report.toString().replace("\r", ""));

	}

}
