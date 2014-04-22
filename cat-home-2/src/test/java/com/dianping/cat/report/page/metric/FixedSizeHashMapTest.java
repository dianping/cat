package com.dianping.cat.report.page.metric;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.Test;

public class FixedSizeHashMapTest {
	@Test
	public void testHashMap() {
		Map<String, String> testMap = new LinkedHashMap<String, String>(1000) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<String, String> eldest) {
				return size() > 1000;
			}
		};

		for (int i = 0; i < 10000; i++) {
			testMap.put(String.valueOf(i), String.valueOf(i));
		}
		Assert.assertEquals(1000, testMap.size());
	}
}
