package com.dianping.cat.message.internal;

import java.util.concurrent.locks.LockSupport;

import junit.framework.Assert;

import org.junit.Test;

public class MillisSecondTimerTest {
	@Test
	public void test() {
		MilliSecondTimer.initialize();

		for (int i = 1; i <= 100; i++) {
			LockSupport.parkNanos(1000 * 1000); // 1 ms

			long t1 = System.currentTimeMillis();
			long t2 = MilliSecondTimer.currentTimeMillis();

			// 15~16 ms is one tick in Windows system
			Assert.assertTrue("failed on loop " + i + ": " + (t2 - t1), t2 - t1 <= 16);
		}
	}
}
