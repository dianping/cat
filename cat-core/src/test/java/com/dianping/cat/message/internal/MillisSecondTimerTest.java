package com.dianping.cat.message.internal;

import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.junit.Test;

public class MillisSecondTimerTest {
	@Test
	public void test() {
		MilliSecondTimer.initialize();

		long t1 = MilliSecondTimer.currentTimeMillis();

		for (int i = 1; i <= 100; i++) {
			LockSupport.parkUntil(t1 + i);

			long t2 = MilliSecondTimer.currentTimeMillis();

			Assert.assertTrue("failed on loop " + i + ": " + (t2 - t1), t2 - t1 <= i + 1);
		}
	}
}
