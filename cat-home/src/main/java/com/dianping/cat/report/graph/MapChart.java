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
import java.util.List;

public class MapChart {

	private String m_title;

	private String m_subTitle;

	private int m_min;

	private int m_max;

	private List<Item> m_dataSeries = new ArrayList<Item>();

	private String m_data;

	public String getData() {
		return m_data;
	}

	public void setData(String data) {
		m_data = data;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getSubTitle() {
		return m_subTitle;
	}

	public void setSubTitle(String subTitle) {
		m_subTitle = subTitle;
	}

	public int getMin() {
		return m_min;
	}

	public void setMin(int min) {
		m_min = min;
	}

	public int getMax() {
		return m_max;
	}

	public void setMax(int max) {
		m_max = max;
	}

	public List<Item> getDataSeries() {
		return m_dataSeries;
	}

	public void setDataSeries(List<Item> items) {
		m_dataSeries = items;
	}

	public static class Item {

		private String m_name;

		private double m_value;

		public Item(String name, double value) {
			m_name = name;
			m_value = value;
		}

		public String getName() {
			return m_name;
		}

		public void setName(String name) {
			m_name = name;
		}

		public double getValue() {
			return m_value;
		}

		public void setValue(Double value) {
			m_value = value;
		}
	}
}