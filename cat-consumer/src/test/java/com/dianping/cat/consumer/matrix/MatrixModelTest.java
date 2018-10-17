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
package com.dianping.cat.consumer.matrix;

import junit.framework.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;

public class MatrixModelTest {
	@Test
	public void testModel() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("matrix.xml"), "utf-8");
		MatrixReport report = DefaultSaxParser.parse(source);
		MatrixReportFilter filter = new MatrixReportFilter();

		filter.setMaxSize(10);
		report.accept(filter);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_result.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}
}
