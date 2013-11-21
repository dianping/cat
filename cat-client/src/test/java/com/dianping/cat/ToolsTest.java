package com.dianping.cat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.site.helper.Stringizers;

public class ToolsTest {

	@Test
	public void testSplitters() {
		String str = "A;B;C;D;E;A;;B;F ";
		List<String> items = Splitters.by(";").noEmptyItem().trim().split(str);
		Assert.assertEquals(8, items.size());

		List<String> emptyItems = Splitters.by(';').trim().split(str);
		Assert.assertEquals(9, emptyItems.size());
	}

	@Test
	public void testStringizers() {
		Item item = new Item("aaa", "bbbbb", "ccccccccc");
		String[] array = { "aaa", "bbbbb", "ccccccccc" };
		List<String> list = Arrays.asList(array);

		item.setArray(array);
		item.setList(list);

		String expected = "{\"a\": \"aaa\", \"array\": [\"aaa\", \"bbbbb\", \"c...c\"], \"b\": \"bbbbb\", \"c\": \"c...c\", \"list\": [\"aaa\", \"bbbbb\", \"c...c\"]}";
		String str = Stringizers.forJson().from(item, 3, 5);
		Assert.assertEquals(expected, str);
	}

	@Test
	public void testStringUtils() {
		Assert.assertEquals(false, StringUtils.isEmpty("aa"));
		Assert.assertEquals(true, StringUtils.isNotEmpty("aa"));
		List<String> strs = new ArrayList<String>();
		String separator = ";";

		strs.add("A");
		strs.add("B");
		String joins = StringUtils.join(strs, separator);
		Assert.assertEquals("A;B", joins);

		String[] array = { "A", "B" };
		Assert.assertEquals("A;B", StringUtils.join(array, separator));
		Assert.assertEquals("AB", StringUtils.trimAll("A\t\n B"));
		Assert.assertEquals("A B", StringUtils.normalizeSpace("A\t\n B"));
	}

	public static class Item {
		private String a;

		private String b;

		private String c;

		private String[] array;

		private List<String> list;

		public Item(String a, String b, String c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		public String getA() {
			return a;
		}

		public String[] getArray() {
			return array;
		}

		public String getB() {
			return b;
		}

		public String getC() {
			return c;
		}

		public List<String> getList() {
			return list;
		}

		public void setArray(String[] array) {
			this.array = array;
		}

		public void setList(List<String> list) {
			this.list = list;
		}
	}

}
