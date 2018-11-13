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
package com.dianping.cat.report.page.transaction.task;

import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;

public class TransactionReportDailyGraphCreator {

	private TransactionReport m_report;

	private int m_length;

	private int m_duration = 1;

	private Date m_start;

	public TransactionReportDailyGraphCreator(TransactionReport transactionReport, int length, Date start) {
		m_report = transactionReport;
		m_length = length;
		m_start = start;
	}

	public void createGraph(TransactionReport from) {
		new TransactionReportVisitor().visitTransactionReport(from);
	}

	class TransactionReportVisitor extends BaseVisitor {

		private Machine m_currentMachine;

		private TransactionType m_currentType;

		private TransactionName m_currentName;

		private int m_day;

		private void buildGraphTrend(GraphTrend graph, long totalCount, long failCount, double sumValue, double avgValue) {
			Long[] count = GraphTrendUtil.parseToLong(graph.getCount(), m_length);
			Long[] fails = GraphTrendUtil.parseToLong(graph.getFails(), m_length);
			Double[] sum = GraphTrendUtil.parseToDouble(graph.getSum(), m_length);
			Double[] avg = GraphTrendUtil.parseToDouble(graph.getAvg(), m_length);

			count[m_day] = totalCount;
			fails[m_day] = failCount;
			sum[m_day] = sumValue;
			avg[m_day] = avgValue;

			graph.setCount(StringUtils.join(count, GraphTrendUtil.GRAPH_SPLITTER));
			graph.setAvg(StringUtils.join(avg, GraphTrendUtil.GRAPH_SPLITTER));
			graph.setSum(StringUtils.join(sum, GraphTrendUtil.GRAPH_SPLITTER));
			graph.setFails(StringUtils.join(fails, GraphTrendUtil.GRAPH_SPLITTER));
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitName(TransactionName name) {
			name.setGraphTrend(null);

			String nameId = name.getId();
			m_currentName = m_currentType.findOrCreateName(nameId);

			GraphTrend graph = m_currentName.getGraphTrend();

			if (graph == null) {
				graph = new GraphTrend();
				graph.setDuration(m_duration);
				m_currentName.setGraphTrend(graph);
			}
			buildGraphTrend(graph, name.getTotalCount(), name.getFailCount(), name.getSum(), name.getAvg());

			super.visitName(name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			Date from = transactionReport.getStartTime();

			m_day = (int) ((from.getTime() - m_start.getTime()) / TimeHelper.ONE_DAY);
			super.visitTransactionReport(transactionReport);
		}

		@Override
		public void visitType(TransactionType type) {
			type.setGraphTrend(null);

			String typeId = type.getId();
			m_currentType = m_currentMachine.findOrCreateType(typeId);

			GraphTrend graph = m_currentType.getGraphTrend();

			if (graph == null) {
				graph = new GraphTrend();
				graph.setDuration(m_duration);
				m_currentType.setGraphTrend(graph);
			}
			buildGraphTrend(graph, type.getTotalCount(), type.getFailCount(), type.getSum(), type.getAvg());

			super.visitType(type);
		}
	}

}
