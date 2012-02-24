package com.dianping.cat.consumer.transaction;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

public class GsonTest {
	private void check(Object obj, String expected) {
		String actual = new Gson().toJson(obj);

		Assert.assertEquals(expected, actual);
	}

	private Map<String, Pojo> mapOfPojo() {
		Map<String, Pojo> map = new HashMap<String, Pojo>();

		map.put("first", new Pojo(1, "x"));
		map.put("second", new Pojo(2, "y"));
		map.put("third", new Pojo(3, "z"));

		return map;
	}

	@Test
	public void test() {
		check(null, "");
		check(1, "1");
		check(1.2, "1.2");
		check(true, "true");
		check("xyz", "\"xyz\"");
		check(new String[] { "x", "y" }, "[\"x\",\"y\"]");
		check(new Pojo(3, null), "{\"x\":3}");
		check(new Pojo(3, "a"), "{\"x\":3,\"y\":\"a\"}");
		check(mapOfPojo(), "{\"second\":{\"x\":2,\"y\":\"y\"}," + //
		      "\"third\":{\"x\":3,\"y\":\"z\"}," + //
		      "\"first\":{\"x\":1,\"y\":\"x\"}}");
	}

	@Test
	@Ignore
	public void testOther() {
		check(new Date(1330079278861L), "\"2012-02-24 18:27:58\"");
		check(Date.class, "\"class java.util.Date\"");
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
