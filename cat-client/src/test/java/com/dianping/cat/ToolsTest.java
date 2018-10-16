/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.site.helper.Splitters;
import com.site.helper.Stringizers;
import junit.framework.Assert;
import org.junit.Test;
import org.unidal.lookup.util.StringUtils;

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
		Map<String, String> map = new LinkedHashMap<String, String>();

		map.put("a", "a");
		map.put("b", "b");
		map.put("c", "c");
		item.setArray(array);
		item.setList(list);
		item.setMap(map);

		String expected = "{\"a\": \"aaa\", \"array\": [\"aaa\", \"bbbbb\", \"c...c\"], \"b\": \"bbbbb\", \"c\": \"c...c\", \"list\": [\"aaa\", \"bbbbb\", \"c...c\"], \"map\": {\"a\": \"a\", \"b\": \"b\", \"c\": \"c\"}}";
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

		private Map<String, String> map;

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

		public void setArray(String[] array) {
			this.array = array;
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

		public void setList(List<String> list) {
			this.list = list;
		}

		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}

	}

}
