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
package com.dianping.cat.report.page.statistics.task.service;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class ClientReportStatistics extends BaseVisitor {

	private ClientReport m_clientReport = new ClientReport(Constants.CAT);

	private String m_domain;

	public ClientReport getClienReport() {
		return m_clientReport;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (Constants.ALL.equals(machine.getIp())) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		Domain domain = m_clientReport.findOrCreateDomain(m_domain);

		domain.incTotalCount(name.getTotalCount());
		domain.incFailureCount(name.getFailCount());
		domain.incSum(name.getSum());
		domain.setFailurePercent(domain.getFailureCount() * 1.0 / domain.getTotalCount());
		domain.setAvg(domain.getSum() / domain.getTotalCount());

		String interf = name.getId();
		Method method = domain.findOrCreateMethod(interf);

		method.incTotalCount(name.getTotalCount());
		method.incFailureCount(name.getFailCount());
		method.incSum(name.getSum());
		method.setFailurePercent(method.getFailureCount() * 1.0 / method.getTotalCount());
		method.setAvg(method.getSum() / method.getTotalCount());
		method.setTimeout(name.getMax());
		super.visitName(name);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_domain = transactionReport.getDomain();

		m_clientReport.setStartTime(transactionReport.getStartTime());
		m_clientReport.setEndTime(transactionReport.getEndTime());
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		if ("PigeonCall".equals(type.getId()) || "Call".equals(type.getId())) {
			super.visitType(type);
		}
	}

}
