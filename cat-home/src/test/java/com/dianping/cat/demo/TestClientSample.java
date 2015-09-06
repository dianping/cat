package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;

public class TestClientSample {

	public void testException() {
		while (true) {
			for (int i = 0; i < 100; i++) {
				Cat.logError(new RuntimeException("this is a test"));
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testMetric() {
		while (true) {
			for (int i = 0; i < 100; i++) {
				Cat.logMetricForCount("test");
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
