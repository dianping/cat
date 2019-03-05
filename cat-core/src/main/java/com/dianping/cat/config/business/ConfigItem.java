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
package com.dianping.cat.config.business;

public class ConfigItem {
	private int m_count;

	private double m_value;

	private boolean m_showCount = false;

	private boolean m_showAvg = false;

	private boolean m_showSum = false;

	private String m_title;

	private double viewOrder = 1;

	public int getCount() {
		return m_count;
	}

	public ConfigItem setCount(int count) {
		m_count = count;
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public ConfigItem setTitle(String title) {
		m_title = title;
		return this;
	}

	public double getValue() {
		return m_value;
	}

	public ConfigItem setValue(double value) {
		m_value = value;
		return this;
	}

	public boolean isShowAvg() {
		return m_showAvg;
	}

	public ConfigItem setShowAvg(boolean showAvg) {
		m_showAvg = showAvg;
		return this;
	}

	public boolean isShowCount() {
		return m_showCount;
	}

	public ConfigItem setShowCount(boolean showCount) {
		m_showCount = showCount;
		return this;
	}

	public boolean isShowSum() {
		return m_showSum;
	}

	public ConfigItem setShowSum(boolean showSum) {
		m_showSum = showSum;
		return this;
	}

	public double getViewOrder() {
		return viewOrder;
	}

	public void setViewOrder(double viewOrder) {
		this.viewOrder = viewOrder;
	}

}
