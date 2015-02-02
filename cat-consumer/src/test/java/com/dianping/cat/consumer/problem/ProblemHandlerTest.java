package com.dianping.cat.consumer.problem;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;

public class ProblemHandlerTest {
	private LongExecutionProblemHandler m_handler;

	private int[] m_defaultLongUrlDuration = { 1000, 2000, 3000, 4000, 5000 };

	private Map<String, Integer> m_longUrlThresholds = new HashMap<String, Integer>();

	@Test
	public void testHandler() {
		m_handler = new LongExecutionProblemHandler();

		for (int i = 0; i < 1000; i++) {
			Assert.assertEquals(-1, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 1000; i < 2000; i++) {
			Assert.assertEquals(1000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 2000; i < 3000; i++) {
			Assert.assertEquals(2000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 3000; i < 4000; i++) {
			Assert.assertEquals(3000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 4000; i < 5000; i++) {
			Assert.assertEquals(4000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
		for (int i = 5000; i < 8000; i++) {
			Assert.assertEquals(5000, m_handler.computeLongDuration(i, "domain", m_defaultLongUrlDuration, m_longUrlThresholds));
		}
	}
}
