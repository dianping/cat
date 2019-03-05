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
package com.dianping.cat.report.page.metric.task;

import java.util.List;

public class BaselineConfig {

	private int m_id;

	private String m_key;

	private int m_targetDate;

	private List<Double> m_weights;

	private List<Integer> m_days;

	public List<Integer> getDays() {
		return m_days;
	}

	public void setDays(List<Integer> days) {
		m_days = days;
	}

	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	public int getTargetDate() {
		return m_targetDate;
	}

	public void setTargetDate(int targetDate) {
		m_targetDate = targetDate;
	}

	public List<Double> getWeights() {
		return m_weights;
	}

	public void setWeights(List<Double> weights) {
		m_weights = weights;
	}

}
