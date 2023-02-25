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
package com.dianping.cat.report.task;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TaskHelperTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJoinIntArrayChar() {
		Assert.assertEquals("1,2", TaskHelper.join(new int[] { 1, 2 }, ','));
	}

	@Test
	public void testYesterdayZero() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date yesterdayZero = TaskHelper.yesterdayZero(cal.getTime());
		Assert.assertEquals(cal.getTimeInMillis(), yesterdayZero.getTime() + 3600L * 1000 * 24);
	}

	@Test
	public void testYesterdayZero2() {
		Assert.assertEquals(new Date(1562601600000L), TaskHelper.yesterdayZero(new Date(1562769422000L)));
	}

	@Test
	public void testTodayZero() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		Date todayZero = TaskHelper.todayZero(cal.getTime());
		Assert.assertEquals(cal.getTimeInMillis(), todayZero.getTime() + 1L);
	}

	@PrepareForTest({TaskHelper.class})
	@Test
	public void testTodayZero2() throws Exception {
		Date date = new Date(1562774400000L);
		PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
		Assert.assertEquals(date, TaskHelper.todayZero(null));
	}

	@Test
	public void testTodayZero3() {
		Assert.assertEquals(new Date(1562601600000L), TaskHelper.todayZero(new Date(1562683022000L)));
	}

	@Test
	public void testJoinIntArray() {
		final int[] array = {1, -8, -7, -7, -7, -7, -7, -253968, -7, -24, -24,
				-2147475464, 4088, -2147483647, 9, 33505273, 1, -8};

		Assert.assertNull(TaskHelper.join((int[]) null, '1', 2, 3));

		Assert.assertEquals("", TaskHelper.join(new int[0], '\u0000', 24, 5));
		Assert.assertEquals("", TaskHelper.join(new int[0], '\u0000'));
		Assert.assertEquals("1\u0000-8", TaskHelper.join(array, '\u0000', 16, 17));
	}

	@Test
	public void testJoinDoubleArray() {
		Assert.assertNull(TaskHelper.join((double[]) null, '1', 2, 3));

		Assert.assertEquals("", TaskHelper.join(new double[]{5.1}, '\u0000'));
		Assert.assertEquals("2.1\u00004.5", TaskHelper.join(new double[]{2.1, 4.5}, '\u0000'));
		Assert.assertEquals("", TaskHelper.join(new double[]{5.1, 9.2}, '\u0000', 119, 96));
	}

	@Test
	public void testJoinNumberArray() {
		Assert.assertNull(TaskHelper.join((Number[]) null, '1', 2, 3));

		Assert.assertEquals("", TaskHelper.join(new Number[0], '\u0000'));
		Assert.assertEquals("-100\u0000-891", TaskHelper.join(new Number[]{-100, -891}, '\u0000', 0, 1));
		Assert.assertEquals("", TaskHelper.join(new Number[0], '\u0000', 139, -1744));
	}

	@Test
	public void testJoinThrowsException1() {
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		TaskHelper.join(new Number[0], '\u0000', 112, 1071);
	}

	@Test
	public void testJoinThrowsException2() {
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		TaskHelper.join(new Number[]{-100, -891}, '\u0000', 0, 114);
	}

	@Test
	public void testNextMonthStart() {
		Assert.assertEquals(new Date(1554652800000L), TaskHelper.nextMonthStart(new Date(1552044153000L)));
	}

	@Test
	public void testThisHour() {
		Assert.assertEquals(new Date(1562767200000L), TaskHelper.thisHour(new Date(1562767504000L)));
	}

	@PrepareForTest({TaskHelper.class})
	@Test
	public void testTomorrowZero1() throws Exception {
		Date date = new Date(1562671353000L);
		Date date2 = new Date(1562688000000L);
		PowerMockito.whenNew(Date.class).withAnyArguments().thenReturn(date);
		Assert.assertEquals(date2, TaskHelper.tomorrowZero(null));
	}

	@Test
	public void testTomorrowZero2() {
		Assert.assertEquals(new Date(1562601600000L), TaskHelper.tomorrowZero(new Date(1562596622000L)));
	}
}
