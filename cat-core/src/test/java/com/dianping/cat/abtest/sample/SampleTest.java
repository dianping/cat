package com.dianping.cat.abtest.sample;

import org.junit.Test;

import com.dianping.cat.abtest.ABTest;
import com.dianping.cat.abtest.ABTestName;
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

	public static enum MyABTestId implements ABTestName {
		CASE1("1001"),
		
		CASE2("1002");

		private String m_id;

		private MyABTestId(String id) {
			m_id = id;
		}

		@Override
		public String getValue() {
			return m_id;
		}
	}
}
