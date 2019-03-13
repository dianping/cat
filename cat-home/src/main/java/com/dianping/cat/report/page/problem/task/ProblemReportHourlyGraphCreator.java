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
package com.dianping.cat.report.page.problem.task;

import java.util.Calendar;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportHourlyGraphCreator {
	public static final int DEFAULT_DURATION = 10;

	private ProblemReport m_report;

	private int m_duration;

	public ProblemReportHourlyGraphCreator(ProblemReport problemReport, int duration) {
		m_report = problemReport;

		if (duration > 0 && (60 % duration == 0)) {
			m_duration = duration;
		} else {
			m_duration = DEFAULT_DURATION;
		}
	}

	public void createGraph(ProblemReport from) {
		new ProblemReportVisitor().visitProblemReport(from);
	}

	class ProblemReportVisitor extends BaseVisitor {
		private Machine m_currentMachine;

		private Entity m_currentEntity;

		private Integer[] m_fails = new Integer[60];

		private int m_currentHour;

		private int m_graphLength;

		private void buildGraphTrend(GraphTrend graph) {
			Long[] fails = GraphTrendUtil.parseToLong(graph.getFails(), m_graphLength);

			long failsValue = 0;

			for (int i = 0; i < 60; i++) {
				failsValue += m_fails[i];

				if ((i + 1) % m_duration == 0) {
					int index = m_currentHour * (60 / m_duration) + (i + 1) / m_duration - 1;
					fails[index] = (long) Math.ceil((failsValue + 0.0) / m_duration);

					failsValue = 0;
				}
			}

			graph.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));
		}

		private void initParams() {
			for (int i = 0; i < 60; i++) {
				m_fails[i] = 0;
			}
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(problemReport.getStartTime());

			m_currentHour = cal.get(Calendar.HOUR_OF_DAY);
			m_graphLength = (60 / m_duration) * 24;
			super.visitProblemReport(problemReport);
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitEntity(Entity entity) {
			String id = entity.getId();
			m_currentEntity = m_currentMachine.findOrCreateEntity(id);

			initParams();
			super.visitEntity(entity);

			GraphTrend graphTrend = m_currentEntity.getGraphTrend();

			if (graphTrend == null) {
				graphTrend = new GraphTrend();
				graphTrend.setDuration(m_duration);
				m_currentEntity.setGraphTrend(graphTrend);
			}
			buildGraphTrend(graphTrend);
		}

		@Override
		public void visitSegment(Segment segment) {
			int index = segment.getId();
			m_fails[index] += segment.getCount();
		}
	}
}
