package com.dianping.cat.report.task;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskHelperTest {

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

}
