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
package com.dianping.cat.report.page.statistics.task.utilization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public class TransactionReportVisitor extends BaseVisitor {

	private static final String MEMCACHED = "Cache.memcached";

	private String m_domain;

	private UtilizationReport m_report;

	private Set<String> m_types = new HashSet<String>();

	private Map<Integer, Long> m_counts = new HashMap<Integer, Long>();

	public TransactionReportVisitor() {
		m_types.add("URL");
		m_types.add("Service");
		m_types.add("PigeonService");
		m_types.add("Call");
		m_types.add("PigeonCall");
		m_types.add("SQL");
		m_types.add(MEMCACHED);
	}

	private void copyAttribute(TransactionType type, ApplicationState state) {
		long newTotal = state.getCount() + type.getTotalCount();

		if (newTotal > 0) {
			state.setAvg95((state.getCount() * state.getAvg95() + type.getTotalCount() * type.getLine95Value()) / newTotal);
		}
		state.setSum(state.getSum() + type.getSum());
		state.setFailureCount(state.getFailureCount() + type.getFailCount());
		state.setCount(newTotal);

		if (state.getCount() > 0) {
			state.setFailurePercent(state.getFailureCount() * 1.0 / state.getCount());
			state.setAvg(state.getSum() * 1.0 / state.getCount());
		}
	}

	public TransactionReportVisitor setUtilizationReport(UtilizationReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();

		if (Constants.ALL.equals(ip)) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitRange(Range range) {
		long count = range.getCount();
		int value = range.getValue();
		Long old = m_counts.get(value);

		if (old == null) {
			m_counts.put(value, count);
		} else {
			m_counts.put(value, count + old);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_domain = transactionReport.getDomain();
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		String typeName = type.getId();
		Domain domain = m_report.findOrCreateDomain(m_domain);

		if ("Service".equals(typeName)) {
			typeName = "PigeonService";
		} else if ("Call".equals(typeName)) {
			typeName = "PigeonCall";
		} else if (typeName.startsWith(MEMCACHED)) {
			typeName = MEMCACHED;
		}
		ApplicationState applicationState = null;

		if (m_types.contains(typeName)) {
			applicationState = domain.findOrCreateApplicationState(typeName);
			copyAttribute(type, applicationState);
		}
		super.visitType(type);

		if (applicationState != null) {
			long max = 0;

			for (Entry<Integer, Long> entry : m_counts.entrySet()) {
				long value = entry.getValue();

				if (value > max) {
					max = value;
				}
			}
			applicationState.setMaxQps(max * 1.0 / (5 * 60));
		}

		m_counts.clear();
	}

}
