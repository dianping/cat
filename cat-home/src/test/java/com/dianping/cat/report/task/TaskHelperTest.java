package com.dianping.cat.report.task;

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

}
