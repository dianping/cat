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
package com.dianping.cat.report.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dianping.cat.helper.JsonBuilder;

public class PieChart {

	private String m_title;

	private List<Item> m_items = new ArrayList<Item>();

	private transient int m_size = 30;

	public void addItems(List<Item> items) {
		Collections.sort(items, new ItemCompartor());
		int size = items.size();

		if (size <= m_size) {
			m_items = items;
		} else {
			for (int i = 0; i < m_size; i++) {
				m_items.add(items.get(i));
			}
			Item item = new Item().setTitle("Other");

			double sum = 0;
			for (int i = m_size; i < size; i++) {
				Item temp = items.get(i);

				sum += temp.getNumber();
			}
			m_items.add(item.setNumber(sum));
		}
	}

	public List<Item> getItems() {
		return m_items;
	}

	public String getJsonString() {
		return new JsonBuilder().toJson(this);
	}

	public String getTitle() {
		return m_title;
	}

	public PieChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public PieChart setMaxSize(int size) {
		m_size = size;
		return this;
	}

	public static class Item {
		private int m_id;

		private String m_title;

		private double m_number;

		public int getId() {
			return m_id;
		}

		public void setId(int id) {
			m_id = id;
		}

		public double getNumber() {
			return m_number;
		}

		public Item setNumber(double number) {
			m_number = number;
			return this;
		}

		public String getTitle() {
			return m_title;
		}

		public Item setTitle(String title) {
			m_title = title;
			return this;
		}
	}

	public static class ItemCompartor implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			return (int) (o2.getNumber() - o1.getNumber());
		}
	}
}
