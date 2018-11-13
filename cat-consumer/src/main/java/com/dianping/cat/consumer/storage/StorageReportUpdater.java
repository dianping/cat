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
package com.dianping.cat.consumer.storage;

import java.util.Set;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;

@Named
public class StorageReportUpdater {

	public void updateStorageIds(String id, Set<String> ids, StorageReport report) {
		String type = id.substring(id.lastIndexOf("-") + 1);
		Set<String> reportIds = report.getIds();
		reportIds.clear();

		for (String myId : ids) {
			if (myId.endsWith(type)) {
				String prefix = myId.substring(0, myId.lastIndexOf("-"));

				reportIds.add(prefix);
			}
		}
	}

	public void updateStorageReport(StorageReport report, StorageUpdateItem param) {
		Transaction t = param.getTransaction();
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		long duration = t.getDurationInMillis();
		Domain d = report.findOrCreateMachine(param.getIp()).findOrCreateDomain(param.getDomain());
		Operation operation = d.findOrCreateOperation(param.getMethod());
		Segment segment = operation.findOrCreateSegment(min);

		report.addIp(param.getIp());
		report.addOp(param.getMethod());

		operation.incCount();
		operation.incSum(duration);
		operation.setAvg(operation.getSum() / operation.getCount());

		segment.incCount();
		segment.incSum(duration);
		segment.setAvg(segment.getSum() / segment.getCount());

		if (!t.isSuccess()) {
			operation.incError();
			segment.incError();
		}
		if (duration > param.getThreshold()) {
			operation.incLongCount();
			segment.incLongCount();
		}
	}

	public static class StorageUpdateItem {

		private String m_ip;

		private String m_domain;

		private String m_method;

		private Transaction m_transaction;

		private long m_threshold;

		public String getDomain() {
			return m_domain;
		}

		public StorageUpdateItem setDomain(String domain) {
			m_domain = domain;
			return this;
		}

		public String getIp() {
			return m_ip;
		}

		public StorageUpdateItem setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public String getMethod() {
			return m_method;
		}

		public StorageUpdateItem setMethod(String method) {
			m_method = method;
			return this;
		}

		public long getThreshold() {
			return m_threshold;
		}

		public StorageUpdateItem setThreshold(long threshold) {
			m_threshold = threshold;
			return this;
		}

		public Transaction getTransaction() {
			return m_transaction;
		}

		public StorageUpdateItem setTransaction(Transaction transaction) {
			m_transaction = transaction;
			return this;
		}
	}

}
