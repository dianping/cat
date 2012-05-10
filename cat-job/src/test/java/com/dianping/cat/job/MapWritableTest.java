package com.dianping.cat.job;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.job.MapWritable.KeyValueInput;
import com.dianping.cat.job.MapWritable.KeyValueOutput;

public class MapWritableTest {
	private void checkInput(String source, String... expectedPairs) {
		KeyValueInput input = new KeyValueInput(source);
		int expectedIndex = 0;

		while (input.next()) {
			String key = input.getKey();
			String value = input.getValue();
			String expectedKey = expectedIndex < expectedPairs.length ? expectedPairs[expectedIndex++] : null;
			String expectedValue = expectedIndex < expectedPairs.length ? expectedPairs[expectedIndex++] : null;

			Assert.assertEquals(expectedKey, key);
			Assert.assertEquals(expectedValue, value);
		}

		if (expectedIndex < expectedPairs.length) {
			Assert.fail(String.format("Pairs(%s) is not found in the source!",
			      Arrays.asList(expectedPairs).subList(expectedIndex, expectedPairs.length)));
		}
	}

	private void checkOutput(String expected, String... keyValuePairs) {
		KeyValueOutput output = new KeyValueOutput();
		int len = keyValuePairs.length;

		for (int i = 0; i < len; i += 2) {
			String key = keyValuePairs[i];
			String value = i + 1 < len ? keyValuePairs[i + 1] : null;

			output.add(key, value);
		}

		Assert.assertEquals(expected, output.toString());
	}

	@Test
	public void testInput() {
		checkInput("k1=v1&k2=v2&k3=v3", "k1", "v1", "k2", "v2", "k3", "v3");
		checkInput("k1=v1&k2=v2&k3=v3&k4", "k1", "v1", "k2", "v2", "k3", "v3", "k4");

		checkInput("k1=v1&k2\\=k2=v2\\&v2&k3=v3\\r\\nv3&k4=v4\\\\v4&k5", "k1", "v1", "k2=k2", "v2&v2", "k3",
		      "v3\r\nv3", "k4", "v4\\v4", "k5");
	}

	@Test
	public void testOutput() {
		checkOutput("k1=v1&k2=v2&k3=v3", "k1", "v1", "k2", "v2", "k3", "v3");
		checkOutput("k1=v1&k2=v2&k3=v3&k4", "k1", "v1", "k2", "v2", "k3", "v3", "k4");

		checkOutput("k1=v1&k2\\=k2=v2\\&v2&k3=v3\\r\\nv3&k4=v4\\\\v4&k5", "k1", "v1", "k2=k2", "v2&v2", "k3",
		      "v3\r\nv3", "k4", "v4\\v4", "k5");
	}
}
