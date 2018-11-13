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
package com.dianping.cat.report.page.transaction.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class AllMachineMerger extends BaseVisitor {

	public TransactionReport m_report;

	public String m_currentType;

	public String m_currentName = null;

	public Integer m_currentRange;

	public Integer m_currentDuration;

	public TransactionReportMerger m_merger = new TransactionReportMerger(new TransactionReport());

	public TransactionReport getReport() {
		return m_report;
	}

	@Override
	public void visitDuration(Duration duration) {
		m_currentDuration = duration.getValue();
		Duration temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
								.findOrCreateName(m_currentName).findOrCreateDuration(m_currentDuration);

		m_merger.mergeDuration(temp, duration);

		super.visitDuration(duration);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_report.findOrCreateMachine(Constants.ALL);
		super.visitMachine(machine);
	}

	@Override
	public void visitName(TransactionName name) {
		m_currentName = name.getId();
		TransactionName temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
								.findOrCreateName(m_currentName);

		m_merger.mergeName(temp, name);
		super.visitName(name);
		m_currentName = null;
	}

	@Override
	public void visitRange(Range range) {
		m_currentRange = range.getValue();
		Range temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
								.findOrCreateName(m_currentName).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(temp, range);
		super.visitRange(range);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_report = new TransactionReport(transactionReport.getDomain());
		m_report.setStartTime(transactionReport.getStartTime());
		m_report.setEndTime(transactionReport.getEndTime());
		m_report.getIps().addAll(transactionReport.getIps());

		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		m_currentType = type.getId();
		TransactionType temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType);

		m_merger.mergeType(temp, type);
		super.visitType(type);
	}

	@Override
	public void visitGraph(Graph graph) {
		int duration = graph.getDuration();
		Graph temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
								.findOrCreateName(m_currentName).findOrCreateGraph(duration);

		m_merger.mergeGraph(temp, graph);
		super.visitGraph(graph);
	}

	@Override
	public void visitGraph2(Graph2 graph2) {
		int duration = graph2.getDuration();
		Graph2 temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
								.findOrCreateGraph2(duration);

		m_merger.mergeGraph2(temp, graph2);
		super.visitGraph2(graph2);
	}

	@Override
	public void visitGraphTrend(GraphTrend graph) {
		GraphTrend temp = null;

		if (m_currentName != null) {
			TransactionName name = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
									.findOrCreateName(m_currentName);
			temp = name.getGraphTrend();

			if (temp == null) {
				temp = new GraphTrend();
				temp.setDuration(graph.getDuration());
				name.setGraphTrend(temp);
			}
		} else {
			TransactionType type = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType);
			temp = type.getGraphTrend();

			if (temp == null) {
				temp = new GraphTrend();
				temp.setDuration(graph.getDuration());
				type.setGraphTrend(temp);
			}
		}

		m_merger.mergeGraphTrend(temp, graph);
		super.visitGraphTrend(graph);
	}
}
