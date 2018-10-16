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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.cat.helper.JsonBuilder;

public class LineChart {

	private transient SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	private int m_size;

	private long m_step;

	private String m_start;

	private List<String> m_subTitles = new ArrayList<String>();

	private String m_title;

	private String m_unit = "Value/分钟";

	private String m_id;

	private String m_htmlTitle;

	private List<double[]> m_values = new ArrayList<double[]>();

	private List<Double[]> m_valueObjects = new ArrayList<Double[]>();

	private List<Map<Long, Double>> m_datas = new ArrayList<Map<Long, Double>>();

	private double[] m_ylabel;

	private Double m_minYlabel = 0D;

	private Double m_maxYlabel;

	private long m_minTickInterval;

	private boolean m_yEnabled = true;

	public LineChart() {
	}

	public LineChart add(String title, Double[] value) {
		m_subTitles.add(title);
		m_valueObjects.add(value);
		return this;
	}

	public LineChart add(String title, Map<Long, Double> data) {
		m_datas.add(data);
		m_subTitles.add(title);
		return this;
	}

	public LineChart addSubTitle(String title) {
		m_subTitles.add(title);
		return this;
	}

	public LineChart addValue(double[] value) {
		m_values.add(value);
		return this;
	}

	public List<Map<Long, Double>> getDatas() {
		return m_datas;
	}

	public LineChart setDatas(List<Map<Long, Double>> datas) {
		m_datas = datas;
		return this;
	}

	public String getHtmlTitle() {
		if (m_htmlTitle == null) {
			return m_title;
		} else {
			return m_htmlTitle;
		}
	}

	public LineChart setHtmlTitle(String htmlTitle) {
		m_htmlTitle = htmlTitle;
		return this;
	}

	public String getId() {
		return m_id;
	}

	public LineChart setId(String id) {
		m_id = id;
		return this;
	}

	public String getJsonString() {
		String json = new JsonBuilder().toJson(this);
		return json;
	}

	public Double getMaxYlabel() {
		return m_maxYlabel;
	}

	public LineChart setMaxYlabel(Double maxYlabel) {
		m_maxYlabel = maxYlabel;
		return this;
	}

	public Double getMinYlable() {
		return m_minYlabel;
	}

	public LineChart setMinYlable(Double minYlable) {
		m_minYlabel = minYlable;
		return this;
	}

	public int getSize() {
		return m_size;
	}

	public LineChart setSize(int size) {
		m_size = size;
		return this;
	}

	public String getStart() {
		return m_start;
	}

	public LineChart setStart(Date start) {
		m_start = m_sdf.format(start);
		return this;
	}

	public long getStep() {
		return m_step;
	}

	public LineChart setStep(long step) {
		m_step = step;
		return this;
	}

	public List<String> getSubTitles() {
		return m_subTitles;
	}

	public LineChart setSubTitles(List<String> subTitles) {
		m_subTitles = subTitles;
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public LineChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public String getUnit() {
		return m_unit;
	}

	public LineChart setUnit(String unit) {
		m_unit = unit;
		return this;
	}

	public List<Double[]> getValueObjects() {
		return m_valueObjects;
	}

	public List<double[]> getValues() {
		return m_values;
	}

	public LineChart setValues(List<double[]> values) {
		m_values = values;
		return this;
	}

	public double[] getValues(int index) {
		int size = m_values.size();

		if (index > size) {
			return null;
		} else {
			return m_values.get(index);
		}
	}

	public double[] getYlable() {
		return m_ylabel;
	}

	public LineChart setYlable(double[] ylable) {
		if (ylable == null) {
			m_ylabel = new double[0];
		} else {
			m_ylabel = Arrays.copyOf(ylable, ylable.length);
		}
		return this;
	}

	public boolean isyEnabled() {
		return m_yEnabled;
	}

	public void setyEnabled(boolean yEnabled) {
		m_yEnabled = yEnabled;
	}

	public long getMinTickInterval() {
		return m_minTickInterval;
	}

	public void setMinTickInterval(long minTickInterval) {
		m_minTickInterval = minTickInterval;
	}

	public double queryMinYlable(final Map<String, Double[]> datas) {
		double min = Double.MAX_VALUE;

		for (Double[] data : datas.values()) {
			if (data.length > 0) {
				List<Double> dataList = Arrays.asList(data);
				double tmp = Collections.min(dataList);

				if (min > tmp) {
					min = tmp;
				}
			}
		}
		return min;
	}

	public double queryMinYlable(final double[] datas) {
		double min = Double.MAX_VALUE;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i] < min) {
				min = datas[i];
			}
		}
		return min;
	}

	public String toString() {
		return new JsonBuilder().toJson(this);
	}
}
