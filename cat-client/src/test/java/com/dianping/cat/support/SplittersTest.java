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
package com.dianping.cat.support;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SplittersTest {
	@Test
	public void testList() {
		String str = "A;B;C;D;E;A;;B;F ";
		List<String> items = Splitters.by(";").noEmptyItem().trim().split(str);
		Assert.assertEquals(8, items.size());

		List<String> emptyItems = Splitters.by(';').trim().split(str);
		Assert.assertEquals(9, emptyItems.size());
	}
}
