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
package com.dianping.cat.report.page.storage.display;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageSorter {

	private StorageReport m_report;

	private String m_operation;

	private String m_type;

	private boolean m_sortValue = true;

	public StorageSorter(StorageReport report, String sort) {
		m_report = report;
		int index = sort.indexOf(";");

		if (index < 0) {
			m_sortValue = false;
		} else {
			m_operation = sort.substring(0, index);
			m_type = sort.substring(index + 1);
		}
	}

	public Machine getMachine() {
		Machine machine = new Machine();

		if (!m_report.getMachines().isEmpty()) {
			machine = m_report.getMachines().values().iterator().next();
		}
		return machine;
	}

	public StorageReport getSortedReport() {
		Machine machine = getMachine();
		Map<String, Domain> domains = machine.getDomains();
		List<Entry<String, Domain>> tmp = new LinkedList<Entry<String, Domain>>(domains.entrySet());
		Map<String, Domain> results = new LinkedHashMap<String, Domain>();

		Collections.sort(tmp, new StorageComparator());

		for (Entry<String, Domain> entry : tmp) {
			results.put(entry.getKey(), entry.getValue());
		}
		machine.getDomains().clear();
		machine.getDomains().putAll(results);
		return m_report;
	}

	public class StorageComparator implements Comparator<Entry<String, Domain>> {

		@Override
		public int compare(Entry<String, Domain> o1, Entry<String, Domain> o2) {
			String domain1 = o1.getKey();
			String domain2 = o2.getKey();

			if (m_sortValue) {
				if (Constants.ALL.equals(domain1)) {
					return -1;
				} else if (Constants.ALL.equals(domain2)) {
					return 1;
				} else {
					Operation op1 = o1.getValue().findOrCreateOperation(m_operation);
					Operation op2 = o2.getValue().findOrCreateOperation(m_operation);

					return sortValue(op1, op2);
				}
			} else {
				return sortDomain(domain1, domain2);
			}
		}

		private int sortDomain(String o1, String o2) {
			if (Constants.ALL.equals(o1)) {
				return -1;
			}
			if (Constants.ALL.equals(o2)) {
				return 1;
			}

			return o1.compareTo(o2);
		}

		private int sortValue(Operation op1, Operation op2) {
			if (StorageConstants.COUNT.equals(m_type)) {
				long count1 = op1.getCount();
				long count2 = op2.getCount();

				return count2 > count1 ? 1 : (count2 < count1 ? -1 : 0);
			} else if (StorageConstants.LONG.equals(m_type)) {
				long long1 = op1.getLongCount();
				long long2 = op2.getLongCount();

				return long2 > long1 ? 1 : (long2 < long1 ? -1 : 0);
			} else if (StorageConstants.AVG.equals(m_type)) {
				double avg1 = op1.getAvg();
				double avg2 = op2.getAvg();

				return avg2 > avg1 ? 1 : (avg2 < avg1 ? -1 : 0);
			} else if (StorageConstants.ERROR.equals(m_type)) {
				long error1 = op1.getError();
				long error2 = op2.getError();

				return error2 > error1 ? 1 : (error2 < error1 ? -1 : 0);
			} else {
				return 0;
			}
		}
	}

}
