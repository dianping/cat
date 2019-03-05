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
package com.dianping.cat.report.page.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.report.graph.svg.AbstractGraphPayload;

abstract class AbstractPayload extends AbstractGraphPayload {
	private final EventName m_name;

	public AbstractPayload(String title, String axisXLabel, String axisYLabel, EventName name) {
		super(title, axisXLabel, axisYLabel);

		m_name = name;
	}

	@Override
	public String getAxisXLabel(int index) {
		if (index % 5 == 0 && index < 61) {
			return String.valueOf(index);
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
		return (int) (super.getDisplayWidth() * 0.7);
	}

	protected EventName getEventName() {
		return m_name;
	}

	@Override
	public String getIdPrefix() {
		return m_name.getId() + "_" + super.getIdPrefix();
	}

	@Override
	public int getWidth() {
		return super.getWidth() + 120;
	}

	@Override
	public boolean isStandalone() {
		return false;
	}
}

final class FailurePayload extends AbstractPayload {
	public FailurePayload(String title, String axisXLabel, String axisYLabel, EventName name) {
		super(title, axisXLabel, axisYLabel, name);
	}

	@Override
	public int getOffsetX() {
		return getDisplayWidth();
	}

	@Override
	protected double[] loadValues() {
		double[] values = new double[60];

		for (Range range : getEventName().getRanges().values()) {
			int value = range.getValue();

			values[value] += range.getFails();
		}

		return values;
	}
}

final class HitPayload extends AbstractPayload {
	public HitPayload(String title, String axisXLabel, String axisYLabel, EventName name) {
		super(title, axisXLabel, axisYLabel, name);
	}

	@Override
	protected double[] loadValues() {
		double[] values = new double[60];

		for (Range range : getEventName().getRanges().values()) {
			int value = range.getValue();

			values[value] += range.getCount();
		}

		return values;
	}
}