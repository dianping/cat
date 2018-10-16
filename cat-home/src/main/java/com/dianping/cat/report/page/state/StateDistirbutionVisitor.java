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
package com.dianping.cat.report.page.state;

import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class StateDistirbutionVisitor extends BaseVisitor {

	private Map<String, Double> m_distribute = new HashMap<String, Double>();

	private String m_currentIp;

	private String m_domain = "";

	private String m_attribute = "";

	public StateDistirbutionVisitor(String key) {
		int index = key.indexOf(":");

		if (index > -1) {
			m_domain = key.substring(0, index);
			m_attribute = key.substring(index + 1);
		} else {
			m_attribute = key;
		}
	}

	public Map<String, Double> getDistribute() {
		return m_distribute;
	}

	private void incDistribute(String ip, double value) {
		if (value > 0) {
			Double old = m_distribute.get(ip);

			if (old == null) {
				old = new Double(0);
			}
			m_distribute.put(ip, old + value);
		}
	}

	private double queryValue(String key, Machine machine) {
		double value = 0;
		if (key.equalsIgnoreCase("total")) {
			value = machine.getTotal();
		} else if (key.equalsIgnoreCase("totalLoss")) {
			value = machine.getTotalLoss();
		} else if (key.equalsIgnoreCase("avgTps")) {
			value = machine.getAvgTps();
		} else if (key.equalsIgnoreCase("maxTps")) {
			value = machine.getMaxTps();
		} else if (key.equalsIgnoreCase("dump")) {
			value = machine.getDump();
		} else if (key.equalsIgnoreCase("dumpLoss")) {
			value = machine.getDumpLoss();
		} else if (key.equalsIgnoreCase("pigeonTimeError")) {
			value = machine.getPigeonTimeError();
		} else if (key.equalsIgnoreCase("networkTimeError")) {
			value = machine.getNetworkTimeError();
		} else if (key.equalsIgnoreCase("blockTotal")) {
			value = machine.getBlockTotal();
		} else if (key.equalsIgnoreCase("blockLoss")) {
			value = machine.getBlockLoss();
		} else if (key.equalsIgnoreCase("blockTime")) {
			value = machine.getBlockTime() * 1.0 / 60 / 1000;
		} else if (key.equalsIgnoreCase("size")) {
			value = machine.getSize() / 1024 / 1024;
		} else if (key.equalsIgnoreCase("delayAvg")) {
			if (machine.getDelayCount() > 0) {
				value = machine.getDelaySum() / machine.getDelayCount();
			}
		}
		return value;
	}

	private double queryValue(String key, ProcessDomain processDomain) {
		double value = 0;
		if (key.equalsIgnoreCase("total")) {
			value = processDomain.getTotal();
		} else if (key.equalsIgnoreCase("totalLoss")) {
			value = processDomain.getTotalLoss();
		} else if (key.equalsIgnoreCase("size")) {
			value = processDomain.getSize() / 1024 / 1024;
		}
		return value;
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();

		if (StringUtils.isEmpty(m_domain)) {
			incDistribute(m_currentIp, queryValue(m_attribute, machine));
		} else {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		if (m_domain.equals(processDomain.getName())) {
			incDistribute(m_currentIp, queryValue(m_attribute, processDomain));
		}
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		super.visitStateReport(stateReport);
	}

}
