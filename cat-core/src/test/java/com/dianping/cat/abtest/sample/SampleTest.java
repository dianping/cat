package com.dianping.cat.abtest.sample;

import org.junit.Test;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.ABTestManager;

public class SampleTest {
	public static ABTest abtest1 = ABTestManager.getTest(MyABTestId.CASE1);
	public static ABTest abtest2 = ABTestManager.getTest(MyABTestId.CASE2);

	@Test
	public void usage() {
		// some initialization for case 1

		if (abtest1.isGroupA()) {
			// Cat.logMetric(...);
		} else if (abtest1.isGroupB()) {
			// Cat.logMetric(...);
		} else {
			// Cat.logMetric(...);
		}

		// some cleanup for case 1
	}

	public static enum MyABTestId implements ABTestId {
		CASE1(1001),
		
		CASE2(1002);

		private int m_id;

		private MyABTestId(int id) {
			m_id = id;
		}

		@Override
		public int getValue() {
			return m_id;
		}
	}
}
