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
package com.dianping.cat.report.page.storage.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.BaseVisitor;

public class AllDomainMerger extends BaseVisitor {

	private StorageReport m_storageReport;

	private String m_currentMachine;

	private String m_currentOperation;

	private String m_currentDomain;

	public StorageReport getStorageReport() {
		return m_storageReport;
	}

	private void mergeOperation(Operation operation, String domain) {
		Operation to = m_storageReport.findOrCreateMachine(m_currentMachine).findOrCreateDomain(domain)
								.findOrCreateOperation(m_currentOperation);

		to.setCount(to.getCount() + operation.getCount());
		to.setLongCount(to.getLongCount() + operation.getLongCount());
		to.setError(to.getError() + operation.getError());
		to.setSum(to.getSum() + operation.getSum());
		to.setAvg(to.getCount() > 0 ? to.getSum() / to.getCount() : 0);
	}

	private void mergeSegment(Segment segment, String domain) {
		Segment to = m_storageReport.findOrCreateMachine(m_currentMachine).findOrCreateDomain(domain)
								.findOrCreateOperation(m_currentOperation).findOrCreateSegment(segment.getId());

		to.setCount(to.getCount() + segment.getCount());
		to.setLongCount(to.getLongCount() + segment.getLongCount());
		to.setError(to.getError() + segment.getError());
		to.setSum(to.getSum() + segment.getSum());
		to.setAvg(to.getCount() > 0 ? to.getSum() / to.getCount() : 0);
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getId();
		super.visitDomain(domain);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentMachine = machine.getId();

		super.visitMachine(machine);
	}

	@Override
	public void visitOperation(Operation operation) {
		m_currentOperation = operation.getId();

		mergeOperation(operation, Constants.ALL);
		mergeOperation(operation, m_currentDomain);
		super.visitOperation(operation);
	}

	@Override
	public void visitSegment(Segment segment) {
		mergeSegment(segment, Constants.ALL);
		mergeSegment(segment, m_currentDomain);
		super.visitSegment(segment);
	}

	@Override
	public void visitStorageReport(StorageReport storageReport) {
		m_storageReport = new StorageReport(storageReport.getId());

		m_storageReport.setName(storageReport.getName()).setType(storageReport.getType())
								.setStartTime(storageReport.getStartTime()).setEndTime(storageReport.getEndTime());
		m_storageReport.getIds().addAll(storageReport.getIds());
		m_storageReport.getIps().addAll(storageReport.getIps());
		m_storageReport.getOps().addAll(storageReport.getOps());

		super.visitStorageReport(storageReport);
	}

}
