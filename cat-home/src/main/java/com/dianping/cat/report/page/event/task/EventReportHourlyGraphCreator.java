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
package com.dianping.cat.report.page.event.task;

import java.util.Calendar;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.GraphTrend;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventReportHourlyGraphCreator {

	public static final int DEFAULT_DURATION = 10;

	private EventReport m_report;

	private int m_duration;

	public EventReportHourlyGraphCreator(EventReport eventReport, int duration) {
		m_report = eventReport;

		if (duration > 0 && (60 % duration == 0)) {
			m_duration = duration;
		} else {
			m_duration = DEFAULT_DURATION;
		}
	}

	public void createGraph(EventReport from) {
		new EventReportVisitor().visitEventReport(from);
	}

	class EventReportVisitor extends BaseVisitor {

		private Machine m_currentMachine;

		private EventType m_currentType;

		private EventName m_currentName;

		private Integer[] m_currentNameCount = new Integer[60];

		private Integer[] m_currentNameFails = new Integer[60];

		private Integer[] m_currentTypeCount = new Integer[60];

		private Integer[] m_currentTypeFails = new Integer[60];

		private int m_currentHour;

		private int m_graphLength;

		private void buildGraphTrend(GraphTrend graph, boolean isType) {
			Long[] count = GraphTrendUtil.parseToLong(graph.getCount(), m_graphLength);
			Long[] fails = GraphTrendUtil.parseToLong(graph.getFails(), m_graphLength);

			long countValue = 0;
			long failsValue = 0;

			for (int i = 0; i < 60; i++) {
				countValue += isType ? m_currentTypeCount[i] : m_currentNameCount[i];
				failsValue += isType ? m_currentTypeFails[i] : m_currentNameFails[i];

				if ((i + 1) % m_duration == 0) {
					int index = m_currentHour * (60 / m_duration) + (i + 1) / m_duration - 1;
					count[index] = (long) Math.ceil((countValue + 0.0) / m_duration);
					fails[index] = (long) Math.ceil((failsValue + 0.0) / m_duration);

					countValue = 0;
					failsValue = 0;
				}
			}

			graph.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));
			graph.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));
		}

		private void initNameParams() {
			for (int i = 0; i < 60; i++) {
				m_currentNameCount[i] = 0;
				m_currentNameFails[i] = 0;
			}
		}

		private void initTypeParams() {
			for (int i = 0; i < 60; i++) {
				m_currentTypeCount[i] = 0;
				m_currentTypeFails[i] = 0;
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitName(EventName name) {
			String nameId = name.getId();
			m_currentName = m_currentType.findOrCreateName(nameId);

			initNameParams();
			super.visitName(name);

			GraphTrend graphTrend = m_currentName.getGraphTrend();

			if (graphTrend == null) {
				graphTrend = new GraphTrend();
				graphTrend.setDuration(m_duration);
				m_currentName.setGraphTrend(graphTrend);
			}
			buildGraphTrend(graphTrend, false);
		}

		@Override
		public void visitRange(Range range) {
			int index = range.getValue();
			m_currentNameCount[index] = range.getCount();
			m_currentNameFails[index] = range.getFails();
			m_currentTypeCount[index] += range.getCount();
			m_currentTypeFails[index] += range.getFails();
		}

		@Override
		public void visitEventReport(EventReport eventReport) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(eventReport.getStartTime());

			m_currentHour = cal.get(Calendar.HOUR_OF_DAY);
			m_graphLength = (60 / m_duration) * 24;
			super.visitEventReport(eventReport);
		}

		@Override
		public void visitType(EventType type) {
			String typeId = type.getId();
			m_currentType = m_currentMachine.findOrCreateType(typeId);

			initTypeParams();
			super.visitType(type);

			GraphTrend graphTrend = m_currentType.getGraphTrend();

			if (graphTrend == null) {
				graphTrend = new GraphTrend();
				graphTrend.setDuration(m_duration);
				m_currentType.setGraphTrend(graphTrend);
			}
			buildGraphTrend(graphTrend, true);
		}
	}
}
