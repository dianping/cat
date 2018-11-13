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

import java.util.List;

import com.dianping.cat.helper.JsonBuilder;

public class BarChart {

	private String m_title;

	private String m_serieName;

	private String m_yAxis;

	private List<String> m_xAxis;

	private List<Double> m_values;

	public String getTitle() {
		return m_title;
	}

	public BarChart setTitle(String title) {
		m_title = title;
		return this;
	}

	public String getSerieName() {
		return m_serieName;
	}

	public BarChart setSerieName(String serieName) {
		m_serieName = serieName;
		return this;
	}

	public String getyAxis() {
		return m_yAxis;
	}

	public BarChart setyAxis(String yAxis) {
		m_yAxis = yAxis;
		return this;
	}

	public List<String> getxAxis() {
		return m_xAxis;
	}

	public void setxAxis(List<String> xAxis) {
		m_xAxis = xAxis;
	}

	public String getxAxisJson() {
		return new JsonBuilder().toJson(m_xAxis);
	}

	public String getValuesJson() {
		return new JsonBuilder().toJson(m_values);
	}

	public List<Double> getValues() {
		return m_values;
	}

	public void setValues(List<Double> values) {
		m_values = values;
	}

}
