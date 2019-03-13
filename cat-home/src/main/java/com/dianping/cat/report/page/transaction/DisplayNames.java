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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DisplayNames {

	private List<TransactionNameModel> m_results = new ArrayList<TransactionNameModel>();

	public DisplayNames() {
	}

	public DisplayNames display(String sorted, String type, String ip, TransactionReport report, String queryName) {
		Map<String, TransactionType> types = report.findOrCreateMachine(ip).getTypes();
		TransactionName all = new TransactionName("TOTAL");

		all.setTotalPercent(1);
		if (types != null) {
			TransactionType names = types.get(type);

			if (names != null) {
				for (Entry<String, TransactionName> entry : names.getNames().entrySet()) {
					String transTypeName = entry.getValue().getId();
					boolean isAdd = (queryName == null || queryName.length() == 0 || isFit(queryName, transTypeName));
					if (isAdd) {
						m_results.add(new TransactionNameModel(entry.getKey(), entry.getValue()));
						mergeName(all, entry.getValue());
					}
				}
			}
		}
		if (sorted == null) {
			sorted = "avg";
		}
		Collections.sort(m_results, new TransactionNameComparator(sorted));

		long total = all.getTotalCount();
		for (TransactionNameModel nameModel : m_results) {
			TransactionName transactionName = nameModel.getDetail();
			transactionName.setTotalPercent(transactionName.getTotalCount() / (double) total);
		}
		m_results.add(0, new TransactionNameModel("TOTAL", all));
		return this;
	}

	public List<TransactionNameModel> getResults() {
		return m_results;
	}

	private boolean isFit(String queryName, String transactionName) {
		String[] args = queryName.split("\\|");

		if (args != null) {
			for (String str : args) {
				if (str.length() > 0 && transactionName.toLowerCase().contains(str.trim().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	private void mergeName(TransactionName old, TransactionName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}

		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
		}

		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());
		old.setLine95Value(0);
		old.setLine99Value(0);
		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
			old.setStd(std(old.getTotalCount(), old.getAvg(), old.getSum2(), old.getMax()));
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private double std(long count, double avg, double sum2, double max) {
		double value = sum2 / count - avg * avg;

		if (value <= 0 || count <= 1) {
			return 0;
		} else if (count == 2) {
			return max - avg;
		} else {
			return Math.sqrt(value);
		}
	}

	public static class TransactionNameComparator implements Comparator<TransactionNameModel> {

		private String m_sorted;

		public TransactionNameComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(TransactionNameModel m1, TransactionNameModel m2) {
			if (m_sorted.equals("name") || m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				long count2 = m2.getDetail().getTotalCount();
				long count1 = m1.getDetail().getTotalCount();

				if (count2 > count1) {
					return 1;
				} else if (count2 < count1) {
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

	public static class TransactionNameModel {
		private TransactionName m_detail;

		private String m_type;

		public TransactionNameModel() {
		}

		public TransactionNameModel(String str, TransactionName detail) {
			m_type = str;
			m_detail = detail;
		}

		public TransactionName getDetail() {
			return m_detail;
		}

		public String getName() {
			String id = m_detail.getId();

			try {
				return URLEncoder.encode(id, "utf-8");
			} catch (Exception e) {
				return id;
			}
		}

		public String getType() {
			return m_type;
		}
	}
}
