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
package com.dianping.cat.report.page.heartbeat;

import java.util.Arrays;

import com.dianping.cat.report.graph.svg.AbstractGraphPayload;

public class HeartbeatSvgBuilder extends AbstractGraphPayload {
	private String m_idPrefix;

	private int m_index;

	private String[] m_labels;

	private double[] m_values;

	public HeartbeatSvgBuilder(int index, String title, String axisXLabel, String axisYLabel, double[] values) {
		super(title, axisXLabel, axisYLabel);

		m_idPrefix = title;
		m_index = index;
		m_labels = new String[61];

		for (int i = 0; i <= 60; i++) {
			m_labels[i] = String.valueOf(i);
		}

		if (values == null) {
			m_values = new double[0];
		} else {
			m_values = Arrays.copyOf(values, values.length);
		}
	}

	@Override
	public String getAxisXLabel(int index) {
		if (index % 5 == 0 && index < m_labels.length) {
			return m_labels[index];
		} else {
			return "";
		}
	}

	@Override
	public int getDisplayHeight() {
		return (int) (super.getDisplayHeight() * 0.7);
	}

	@Override
	public int getDisplayWidth() {
		return (int) (super.getDisplayWidth() * 0.66);
	}

	@Override
	public String getIdPrefix() {
		return m_idPrefix;
	}

	@Override
	public int getOffsetX() {
		return m_index % 3 * getDisplayWidth();
	}

	@Override
	public int getOffsetY() {
		return m_index / 3 * (getDisplayHeight() + 20);
	}

	@Override
	public int getWidth() {
		return super.getWidth() + 120;
	}

	@Override
	public boolean isStandalone() {
		return false;
	}

	@Override
	protected double[] loadValues() {
		return m_values;
	}
}