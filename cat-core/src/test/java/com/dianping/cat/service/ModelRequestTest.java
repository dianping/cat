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
package com.dianping.cat.service;

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class ModelRequestTest {

	@Test
	public void test() {
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		String domain = "cat";
		String str = "test";
		ModelRequest request = new ModelRequest(domain, start);

		request.setProperty(str, str);

		Assert.assertEquals(ModelPeriod.CURRENT, request.getPeriod());
		Assert.assertEquals(str, request.getProperty(str));
		Assert.assertEquals("{test=test}", request.getProperties().toString());
		Assert.assertEquals(start, request.getStartTime());
		Assert.assertEquals(domain, request.getDomain());
		Assert.assertEquals("ModelRequest[domain=cat, period=CURRENT, properties={test=test}]", request.toString());

	}
}
