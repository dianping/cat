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

public class ModelPeriodTest {

	@Test
	public void test() {
		long hour = 3600 * 1000L;
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		ModelPeriod period = ModelPeriod.getByTime(start);

		Assert.assertEquals(start, period.getStartTime());
		Assert.assertEquals(period.getStartTime() - hour, ModelPeriod.LAST.getStartTime());
		Assert.assertEquals(true, period.isCurrent());
		Assert.assertEquals(false, period.isHistorical());
		Assert.assertEquals(false, period.isLast());
		Assert.assertEquals(period, ModelPeriod.getByName(period.name(), period));
		Assert.assertEquals(period, ModelPeriod.getByName("other", period));
		Assert.assertEquals(period, ModelPeriod.getByTime(System.currentTimeMillis()));
		Assert.assertEquals(ModelPeriod.LAST, ModelPeriod.getByTime(System.currentTimeMillis() - hour));
		Assert.assertEquals(ModelPeriod.HISTORICAL, ModelPeriod.getByTime(System.currentTimeMillis() - hour * 2));
	}
}
