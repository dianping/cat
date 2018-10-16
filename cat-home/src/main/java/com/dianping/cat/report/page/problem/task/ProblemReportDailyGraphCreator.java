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

import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;

public class ProblemReportDailyGraphCreator {

	private ProblemReport m_report;

	private int m_length;

	private int m_duration = 1;

	private Date m_start;

	public ProblemReportDailyGraphCreator(ProblemReport problemReport, int length, Date start) {
		m_report = problemReport;
		m_length = length;
		m_start = start;
	}

	public void createGraph(ProblemReport from) {
		new ProblemReportVisitor().visitProblemReport(from);
	}

	class ProblemReportVisitor extends BaseVisitor {

		private Machine m_currentMachine;

		private Entity m_currentEntity;

		private int m_day;

		private Long[] m_fails;

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitEntity(Entity entity) {
			entity.setGraphTrend(null);

			String id = entity.getId();
			m_currentEntity = m_currentMachine.findOrCreateEntity(id);

			GraphTrend graphTrend = m_currentEntity.getGraphTrend();

			if (graphTrend == null) {
				graphTrend = new GraphTrend();
				graphTrend.setDuration(m_duration);
				m_currentEntity.setGraphTrend(graphTrend);
			}
			m_fails = GraphTrendUtil.parseToLong(graphTrend.getFails(), m_length);

			super.visitEntity(entity);

			graphTrend.setFails(StringUtils.join(m_fails, GraphTrendUtil.GRAPH_SPLITTER));
		}

		@Override
		public void visitDuration(Duration duration) {
			m_fails[m_day] += duration.getCount();
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			Date from = problemReport.getStartTime();
			m_day = (int) ((from.getTime() - m_start.getTime()) / TimeHelper.ONE_DAY);

			super.visitProblemReport(problemReport);
		}
	}
}
