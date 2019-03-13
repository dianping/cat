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
package com.dianping.cat.consumer.event;

import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventReportTypeAggregator extends BaseVisitor {

	public String m_currentDomain;

	private EventReport m_report;

	private String m_currentType;

	private AllReportConfigManager m_configManager;

	public EventReportTypeAggregator(EventReport report, AllReportConfigManager configManager) {
		m_report = report;
		m_configManager = configManager;
	}

	private void mergeName(EventName old, EventName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private void mergeType(EventType old, EventType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private boolean validateName(String type, String name) {
		return m_configManager.validate(EventAnalyzer.ID, type, name);
	}

	private boolean validateType(String type) {
		return m_configManager.validate(EventAnalyzer.ID, type);
	}

	@Override
	public void visitName(EventName name) {
		if (validateName(m_currentType, name.getId())) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			EventType curentType = machine.findOrCreateType(m_currentType);
			EventName currentName = curentType.findOrCreateName(name.getId());

			mergeName(currentName, name);
		}
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		m_currentDomain = eventReport.getDomain();
		m_report.setStartTime(eventReport.getStartTime());
		m_report.setEndTime(eventReport.getEndTime());
		super.visitEventReport(eventReport);
	}

	@Override
	public void visitType(EventType type) {
		String typeName = type.getId();

		if (validateType(typeName)) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			EventType result = machine.findOrCreateType(typeName);

			m_currentType = typeName;
			mergeType(result, type);

			super.visitType(type);
		}
	}
}