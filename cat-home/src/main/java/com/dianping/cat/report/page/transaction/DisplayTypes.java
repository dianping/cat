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
package com.dianping.cat.report.page.transaction;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DisplayTypes {

	private Set<String> m_ips = new HashSet<String>();

	private List<TransactionTypeModel> m_results = new ArrayList<TransactionTypeModel>();

	public DisplayTypes display(String sorted, String ip, TransactionReport report) {
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
			return this;
		}
		m_ips = report.getIps();

		Map<String, TransactionType> types = machine.getTypes();
		if (types != null) {
			for (Entry<String, TransactionType> entry : types.entrySet()) {
				m_results.add(new TransactionTypeModel(entry.getKey(), entry.getValue()));
			}
		}
		if (sorted == null) {
			sorted = "avg";
		}
		Collections.sort(m_results, new TransactionTypeComparator(sorted));
		return this;
	}

	public Set<String> getIps() {
		return m_ips;
	}

	public List<TransactionTypeModel> getResults() {
		return m_results;
	}

	public static class TransactionTypeComparator implements Comparator<TransactionTypeModel> {

		private String m_sorted;

		public TransactionTypeComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(TransactionTypeModel m1, TransactionTypeModel m2) {
			if (m_sorted.equals("name") || m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				long value = m2.getDetail().getTotalCount() - m1.getDetail().getTotalCount();

				if (value > 0) {
					return 1;
				} else if (value < 0) {
					return -1;
				} else {
					return 0;
				}
			}
			if (m_sorted.equals("failure")) {
				return (int) (m2.getDetail().getFailCount() - m1.getDetail().getFailCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return (int) (m2.getDetail().getFailPercent() * 100 - m1.getDetail().getFailPercent() * 100);
			}
			if (m_sorted.equals("avg")) {
				return (int) (m2.getDetail().getAvg() * 100 - m1.getDetail().getAvg() * 100);
			}
			if (m_sorted.equals("95line")) {
				return (int) (m2.getDetail().getLine95Value() * 100 - m1.getDetail().getLine95Value() * 100);
			}
			if (m_sorted.equals("99line")) {
				return (int) (m2.getDetail().getLine99Value() * 100 - m1.getDetail().getLine99Value() * 100);
			}
			if (m_sorted.equals("min")) {
				return (int) (m2.getDetail().getMin() * 100 - m1.getDetail().getMin() * 100);
			}
			if (m_sorted.equals("max")) {
				return (int) (m2.getDetail().getMax() * 100 - m1.getDetail().getMax() * 100);
			}
			if (m_sorted.equals("std")) {
				return (int) (m2.getDetail().getStd() * 100 - m1.getDetail().getStd() * 100);
			}
			return 0;
		}
	}

	public static class TransactionTypeModel {
		private TransactionType m_detail;

		private String m_type;

		public TransactionTypeModel() {
		}

		public TransactionTypeModel(String str, TransactionType detail) {
			m_type = str;
			m_detail = detail;
		}

		public TransactionType getDetail() {
			return m_detail;
		}

		public String getType() {
			try {
				return URLEncoder.encode(m_type, "utf-8");
			} catch (Exception e) {
				return m_type;
			}
		}
	}
}
