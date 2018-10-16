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
package com.dianping.cat.report.page.storage.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

public class PieChartVisitor extends BaseVisitor {

	private String m_ip;

	private Map<String, Long> m_items = new HashMap<String, Long>();

	public String getPiechartJson() {
		if (m_items.size() > 0) {
			PieChart chart = new PieChart();
			List<Item> items = new ArrayList<Item>();

			for (Entry<String, Long> entry : m_items.entrySet()) {
				Item item = new Item();

				item.setNumber(entry.getValue()).setTitle(entry.getKey());
				items.add(item);
			}
			chart.addItems(items);
			return chart.getJsonString();
		} else {
			return null;
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		m_ip = machine.getId();

		super.visitMachine(machine);
	}

	@Override
	public void visitOperation(Operation operation) {
		long errors = operation.getError();

		if (errors > 0) {
			Long item = m_items.get(m_ip);

			if (item == null) {
				m_items.put(m_ip, errors);
			} else {
				m_items.put(m_ip, item + errors);
			}
		}
	}
}
