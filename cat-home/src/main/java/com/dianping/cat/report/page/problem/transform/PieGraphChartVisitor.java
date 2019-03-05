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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

public class PieGraphChartVisitor extends BaseVisitor {

	private String m_type;

	private String m_status;

	private Map<String, Integer> m_items = new HashMap<String, Integer>();

	private String m_ip;

	public PieGraphChartVisitor(String type, String status) {
		m_type = type;
		m_status = status;
	}

	public PieChart getPieChart() {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (java.util.Map.Entry<String, Integer> entry : m_items.entrySet()) {
			Item item = new Item();

			item.setNumber(entry.getValue()).setTitle(entry.getKey());
			items.add(item);
		}
		chart.addItems(items);

		return chart;
	}

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		Integer old = m_items.get(m_ip);

		if (old == null) {
			m_items.put(m_ip, count);
		} else {
			m_items.put(m_ip, count + old);
		}
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();
		String name = entity.getStatus();

		if (type.equals(m_type)) {
			if (StringUtils.isEmpty(m_status) || name.equals(m_status)) {
				super.visitEntity(entity);
			}
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		m_ip = machine.getIp();

		super.visitMachine(machine);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	@Override
	public void visitThread(JavaThread thread) {
		super.visitThread(thread);
	}

}
