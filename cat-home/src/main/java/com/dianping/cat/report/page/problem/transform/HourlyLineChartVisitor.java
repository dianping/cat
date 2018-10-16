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
package com.dianping.cat.report.page.problem.transform;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;

public class HourlyLineChartVisitor extends BaseVisitor {

	private static final int SIZE = 60;

	private String m_ip;

	private String m_type;

	private String m_state;

	private LineChart m_graphItem = new LineChart();

	private Date m_start;

	private Map<Integer, Integer> m_value = new LinkedHashMap<Integer, Integer>();

	public HourlyLineChartVisitor(String ip, String type, String state, Date start) {
		m_ip = ip;
		m_type = type;
		m_state = state;
		m_start = start;

		m_graphItem.setSize(SIZE);
		m_graphItem.setStep(TimeHelper.ONE_MINUTE);
		m_graphItem.setStart(start);
	}

	private String buildSubTitle() {
		String subTitle = m_type;
		if (!StringUtils.isEmpty(m_state)) {
			subTitle += ":" + m_state;
		}
		return subTitle;
	}

	public LineChart getGraphItem() {
		Double[] value = new Double[SIZE];
		long minute = (System.currentTimeMillis()) / 1000 / 60 % 60;
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		long size = (int) minute + 1;

		if (m_start.getTime() < current) {
			size = SIZE;
		}

		for (int i = 0; i < size; i++) {
			value[i] = 0.0;
		}

		for (int i = 0; i < SIZE; i++) {
			Integer temp = m_value.get(i);

			if (temp != null) {
				value[i] = temp.doubleValue();
			}
		}
		m_graphItem.add(buildSubTitle(), value);
		return m_graphItem;
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();
		String state = entity.getStatus();

		if (m_state == null) {
			if (type.equals(m_type)) {
				super.visitEntity(entity);
			}
		} else {
			if (type.equals(m_type) && state.equals(m_state)) {
				super.visitEntity(entity);
			}
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		if (Constants.ALL.equals(m_ip) || m_ip.equals(machine.getIp())) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		int minute = segment.getId();
		int count = segment.getCount();

		Integer temp = m_value.get(minute);
		if (temp == null) {
			m_value.put(minute, count);
		} else {
			m_value.put(minute, count + temp);
		}
	}

}
