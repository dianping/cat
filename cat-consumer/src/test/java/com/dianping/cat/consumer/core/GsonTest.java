package com.dianping.cat.consumer.core;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

public class GsonTest {
	private void check(Object obj, String expected) {
		String actual = new Gson().toJson(obj);

		Assert.assertEquals(expected, actual);
	}

	Map<String, Object> map(Object forth) {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("first", new Pojo(1, "x"));
		map.put("second", new Pojo(2, "y"));
		map.put("third", new Pojo(3, "z"));
		map.put("forth", forth);

		return map;
	}

	@Test
	public void testSame() {
		check(null, "");
		check(1, "1");
		check(1.2, "1.2");
		check(true, "true");
		check("xyz", "\"xyz\"");
		check(new String[] { "x", "y" }, "[\"x\",\"y\"]");
		check(new Pojo(3, null), "{\"x\":3}");
		check(new Pojo(3, "a"), "{\"x\":3,\"y\":\"a\"}");
		// check(map(null), "{\"second\":{\"x\":2,\"y\":\"y\"},\"third\":{\"x\":3,\"y\":\"z\"},\"first\":{\"x\":1,\"y\":\"x\"}}");
		// check(map(map(map(null))), "{\"forth\":{\"forth\":{\"second\":{\"x\":2,\"y\":\"y\"},\"third\":{\"x\":3,\"y\":\"z\"},\"first\":{\"x\":1,\"y\":\"x\"}},\"second\":{\"x\":2,\"y\":\"y\"},\"third\":{\"x\":3,\"y\":\"z\"},\"first\":{\"x\":1,\"y\":\"x\"}},\"second\":{\"x\":2,\"y\":\"y\"},\"third\":{\"x\":3,\"y\":\"z\"},\"first\":{\"x\":1,\"y\":\"x\"}}");
	}

	public static class Pojo {
		private int x;

		private String y;

		public Pojo(int x, String y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public String getY() {
			return y;
		}
	}
}
