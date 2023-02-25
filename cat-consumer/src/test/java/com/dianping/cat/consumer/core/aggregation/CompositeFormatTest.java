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
package com.dianping.cat.consumer.core.aggregation;

import java.text.ParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.dianping.cat.config.AggregationMessageFormat;
import com.dianping.cat.config.CompositeFormat;

import static org.junit.Assert.assertEquals;

public class CompositeFormatTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void TestParse() throws ParseException {
		AggregationMessageFormat amf = new AggregationMessageFormat("Hello {world}.I am{*}.{md5:8}.");
		CompositeFormat format = new CompositeFormat(amf);

		assertEquals("Hello {world}.I am Jack.{md5:8}.", format.parse("Hello world.I am Jack.balabala."));
	}
}
