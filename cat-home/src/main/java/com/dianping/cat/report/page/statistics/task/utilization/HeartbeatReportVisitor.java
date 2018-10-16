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

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public class HeartbeatReportVisitor extends BaseVisitor {

	private List<Double> m_newGcs;

	private List<Double> m_fullGcs;

	private List<Double> m_loads;

	private String m_domain;

	private UtilizationReport m_report;

	private double computeAvg(List<Double> values) {
		double sum = 0;
		for (Double d : values) {
			sum = sum + d;
		}
		int size = values.size();

		if (size > 0) {
			return sum / values.size();
		} else {
			return 0;
		}
	}

	private double computeDuration(List<Double> values) {
		double sum = 0;
		int size = values.size();
		for (int i = 0; i < size - 1; i++) {
			double first = values.get(i);
			double next = values.get(i + 1);
			double duration = next - first;

			if (duration > 0) {
				sum = sum + duration;
			}
		}
		return sum;
	}

	private double findMax(List<Double> values) {
		double max = 0;

		for (Double d : values) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	public HeartbeatReportVisitor setUtilizationReport(UtilizationReport report) {
		m_report = report;
		return this;
	}

	private void updateMachineState(MachineState state, double value, double maxValue) {
		state.setSum(state.getSum() + value);
		state.setCount(state.getCount() + 1);
		state.setAvg(state.getSum() / state.getCount());

		if (maxValue > state.getAvgMax()) {
			state.setAvgMax(maxValue);
		}
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
		m_domain = heartbeatReport.getDomain();
		super.visitHeartbeatReport(heartbeatReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_newGcs = new ArrayList<Double>();
		m_fullGcs = new ArrayList<Double>();
		m_loads = new ArrayList<Double>();
		super.visitMachine(machine);

		double newgc = computeDuration(m_newGcs);
		double fullgc = computeDuration(m_fullGcs);
		double load = computeAvg(m_loads);

		Domain current = m_report.findOrCreateDomain(m_domain);

		MachineState newGcState = current.findOrCreateMachineState("newGc");
		MachineState fullGcState = current.findOrCreateMachineState("fullGc");
		MachineState loadState = current.findOrCreateMachineState("load");

		updateMachineState(newGcState, newgc, newgc);
		updateMachineState(fullGcState, fullgc, fullgc);
		updateMachineState(loadState, load, findMax(m_loads));
	}

	@Override
	public void visitPeriod(Period period) {
		super.visitPeriod(period);

		m_newGcs.add(period.findOrCreateExtension("GC").findOrCreateDetail("ParNewCount").getValue());
		m_fullGcs.add(period.findOrCreateExtension("GC").findOrCreateDetail("ConcurrentMarkSweepCount").getValue());
		m_loads.add(period.findOrCreateExtension("System").findOrCreateDetail("LoadAverage").getValue());
	}

}
