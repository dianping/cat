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
package com.dianping.cat.report.page.event.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

public class PieGraphChartVisitor extends BaseVisitor {

	private String m_type;

	private String m_name;

	private Map<String, Long> m_items = new HashMap<String, Long>();

	private String m_ip;

	public PieGraphChartVisitor(String type, String name) {
		m_type = type;
		m_name = name;
	}

	public PieChart getPieChart() {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (Entry<String, Long> entry : m_items.entrySet()) {
			Item item = new Item();

			item.setNumber(entry.getValue()).setTitle(entry.getKey());
			items.add(item);
		}
		chart.addItems(items);

		return chart;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (!Constants.ALL.equalsIgnoreCase(machine.getIp())) {
			m_ip = machine.getIp();

			for (EventType type : machine.getTypes().values()) {
				if (m_type != null && m_type.equals(type.getId())) {
					if (StringUtils.isEmpty(m_name)) {
						m_items.put(m_ip, type.getTotalCount());
					} else {
						for (EventName name : type.getNames().values()) {
							if (m_name.equals(name.getId())) {
								m_items.put(m_ip, name.getTotalCount());
								break;
							}
						}
					}
					break;
				}
			}
		}
	}
}
