package com.dianping.cat.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.apache.hadoop.io.Text;
import org.junit.Test;

import com.dianping.cat.job.MapWritable.KeyValueInput;
import com.dianping.cat.job.MapWritable.KeyValueOutput;

public class MapWritableTest {
	private void checkCompareTo(MapWritable writable) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		writable.write(dos);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		MapWritable map = new MapWritable();

		Assert.assertTrue(writable.compareTo(map) != 0);

		map.readFields(dis);

		Assert.assertEquals(0, writable.compareTo(map));
	}

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

	private void checkRead(MapWritable expected, String data) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		Text.writeString(dos, data);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		MapWritable map = new MapWritable();

		map.readFields(dis);

		Assert.assertEquals(0, expected.compareTo(map));
	}

	private void checkWrite(MapWritable writable, String expected) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		DataOutputStream dos = new DataOutputStream(baos);

		writable.write(dos);

		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
		String actual = Text.readString(dis);

		Assert.assertEquals(expected, actual);
	}

	private MapWritable map(Object... keyValueParis) {
		MapWritable map = new MapWritable();

		for (int i = 0; i < keyValueParis.length; i += 2) {
			String key = (String) keyValueParis[i];
			Object value = keyValueParis[i + 1];

			map.put(key, value);
		}

		return map;
	}

	@Test
	public void testCompareTo() throws IOException {
		checkCompareTo(map("name", "Alex Bob", "age", 40, "married", true));
		checkCompareTo(map("name", "Alex Bob", "age", 30, "married", false));
	}

	@Test
	public void testInput() {
		checkInput("k1=v1&k2=v2&k3=v3", "k1", "v1", "k2", "v2", "k3", "v3");
		checkInput("k1=v1&k2=v2&k3=v3&k4", "k1", "v1", "k2", "v2", "k3", "v3", "k4");

		checkInput("k1=v1&k2\\=k2=v2\\&v2&k3=v3\\r\\nv3&k4=v4\\\\v4&k5", "k1", "v1", "k2=k2", "v2&v2", "k3", "v3\r\nv3",
		      "k4", "v4\\v4", "k5");
	}

	@Test
	public void testOutput() {
		checkOutput("k1=v1&k2=v2&k3=v3", "k1", "v1", "k2", "v2", "k3", "v3");
		checkOutput("k1=v1&k2=v2&k3=v3&k4", "k1", "v1", "k2", "v2", "k3", "v3", "k4");

		checkOutput("k1=v1&k2\\=k2=v2\\&v2&k3=v3\\r\\nv3&k4=v4\\\\v4&k5", "k1", "v1", "k2=k2", "v2&v2", "k3", "v3\r\nv3",
		      "k4", "v4\\v4", "k5");
	}

	@Test
	public void testRead() throws IOException {
		checkRead(map("name", "Alex Bob", "age", 40, "married", true), "name=Alex Bob&age=40&married=true");
		checkRead(map("name", "Alex Bob", "age", 30, "married", false), "name=Alex Bob&age=30&married=false");
	}

	@Test
	public void testWrite() throws IOException {
		checkWrite(map("name", "Alex Bob", "age", 40, "married", true), "name=Alex Bob&age=40&married=true");
		checkWrite(map("name", "Alex Bob", "age", 30, "married", false), "name=Alex Bob&age=30&married=false");
	}
}
