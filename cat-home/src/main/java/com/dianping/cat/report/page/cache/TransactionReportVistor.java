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
package com.dianping.cat.report.page.cache;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class TransactionReportVistor extends BaseVisitor {

	private CacheReport m_cacheReport = new CacheReport();

	private String m_currentIp;

	private String m_currentType;

	private EventReport m_eventReport;

	private String m_queryName;

	private String m_sortBy = "missed";

	private String m_type;

	public CacheReport getCacheReport() {
		return m_cacheReport;
	}

	private boolean isFit(String queryName, String methodName) {
		String[] args = queryName.split("\\|");

		if (args != null) {
			for (String str : args) {
				if (str.length() > 0 && methodName.toLowerCase().contains(str.trim().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public TransactionReportVistor setCurrentIp(String currentIp) {
		m_currentIp = currentIp;
		return this;
	}

	public TransactionReportVistor setEventReport(EventReport eventReport) {
		m_eventReport = eventReport;
		return this;
	}

	public TransactionReportVistor setQueryName(String queryName) {
		m_queryName = queryName;
		return this;
	}

	public TransactionReportVistor setSortBy(String sortBy) {
		if (sortBy != null) {
			m_sortBy = sortBy;
		}
		return this;
	}

	public TransactionReportVistor setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (machine.getIp().equalsIgnoreCase(m_currentIp)) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(TransactionName transactionName) {
		String id = transactionName.getId();

		if (!StringUtils.isEmpty(m_type)) {
			if (StringUtils.isEmpty(m_queryName) || isFit(m_queryName, id)) {
				com.dianping.cat.consumer.event.model.entity.Machine machine = m_eventReport.findOrCreateMachine(m_currentIp);
				EventType eventType = machine.findOrCreateType(m_currentType);

				String arrays[] = id.split(":");
				String categroy = arrays[0];
				String method = "";

				if (arrays.length > 1) {
					method = arrays[1];
				}
				EventName eventName = new EventName();
				if (method.equalsIgnoreCase("get")) {
					eventName = eventType.findOrCreateName(categroy + ":missed");
				}
				m_cacheReport.addNewNameItem(transactionName, eventName);
			}
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_cacheReport.setSortBy(m_sortBy);

		super.visitTransactionReport(transactionReport);
		m_cacheReport.setDomain(transactionReport.getDomain());
		m_cacheReport.setStartTime(transactionReport.getStartTime());
		m_cacheReport.setEndTime(transactionReport.getEndTime());
		m_cacheReport.setIps(transactionReport.getIps());
	}

	@Override
	public void visitType(TransactionType transactionType) {
		String id = transactionType.getId();

		if (id.startsWith("Cache.")) {
			if (StringUtils.isEmpty(m_type)) {
				m_currentType = transactionType.getId();
				com.dianping.cat.consumer.event.model.entity.Machine machine = m_eventReport.findOrCreateMachine(m_currentIp);
				EventType eventType = machine.findOrCreateType(m_currentType);
				m_cacheReport.addNewTypeItem(transactionType, eventType);

				super.visitType(transactionType);
			} else if (id.equalsIgnoreCase(m_type)) {
				m_currentType = transactionType.getId();
				super.visitType(transactionType);
			}
		}
	}

}
