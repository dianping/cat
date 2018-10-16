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

import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.helper.Urls;
import org.unidal.tuple.Pair;
import org.unidal.webres.helper.Files;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

public class TestApi {

	private String build711Url(int id) {
		return "http://cat.dianpingoa.com/cat/r/app?op=linechartJson&query1=2015-04-28;" + id + ";;;711;0;;;;;";
	}

	private String build720Url(int id) {
		return "http://cat.dianpingoa.com/cat/r/app?op=linechartJson&query1=2015-04-28;" + id + ";;;720;0;;;;;";
	}

	private String fetchContent(String url) throws Exception {
		InputStream in = Urls.forIO().readTimeout(3000).connectTimeout(3000).openStream(url);

		return Files.forIO().readFrom(in, "utf-8");
	}

	private Pair<Integer, Double> parse(String content) throws ParseException {
		JsonObject obj = new JsonObject(content);
		JsonArray array = obj.getJSONArray("lineChartDetails");
		JsonObject chart = (JsonObject) array.get(0);

		return new Pair<Integer, Double>(chart.getInt("accessNumberSum"), chart.getDouble("successRatio"));
	}

	@Test
	public void test() {
		List<Item> items = new ArrayList<Item>();
		for (int i = 1; i < 711; i++) {
			try {
				String url1 = build711Url(i);
				String url2 = build720Url(i);

				Pair<Integer, Double> pair1 = parse(fetchContent(url1));
				Pair<Integer, Double> pair2 = parse(fetchContent(url2));

				Item item = new Item();

				item.setId(i);
				item.setCount1(pair1.getKey());
				item.setAvg1(pair1.getValue());
				item.setCount2(pair2.getKey());
				item.setAvg2(pair2.getValue());

				items.add(item);
				System.out.print(item.toString());
			} catch (Exception e) {
			}
		}
	}

	private class Item {
		private int m_id;

		private long m_count1;

		private double m_avg1;

		private long m_count2;

		private double m_avg2;

		public void setAvg1(double avg1) {
			m_avg1 = avg1;
		}

		public void setAvg2(double avg2) {
			m_avg2 = avg2;
		}

		public void setCount1(long count1) {
			m_count1 = count1;
		}

		public void setCount2(long count2) {
			m_count2 = count2;
		}

		public void setId(int id) {
			m_id = id;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();

			sb.append(m_id).append("\t");
			sb.append(m_count1).append("\t").append(m_avg1).append("\t");
			sb.append(m_count2).append("\t").append(m_avg2).append("\n");
			return sb.toString();
		}
	}

}
