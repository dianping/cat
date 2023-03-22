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
package com.dianping.cat.consumer.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.consumer.util.InvidStringBuilder;

public class TransactionReportCountFilter extends BaseVisitor {

	private int m_maxTypeLimit;

	private int m_maxNameLimit;

	private String m_domain;

	private int m_lengthLimit = 256;

	public TransactionReportCountFilter(int typeLimit, int nameLimit, int lengthLimit) {
		m_maxNameLimit = nameLimit;
		m_maxTypeLimit = typeLimit;
		m_lengthLimit = lengthLimit;
	}

	private void mergeName(TransactionName old, TransactionName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}
		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
			old.setLongestMessageUrl(other.getLongestMessageUrl());
		}
		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());
		old.setLine95Value(0);
		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private void mergeType(TransactionType old, TransactionType other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (other.getMin() < old.getMin()) {
			old.setMin(other.getMin());
		}
		if (other.getMax() > old.getMax()) {
			old.setMax(other.getMax());
			old.setLongestMessageUrl(other.getLongestMessageUrl());
		}
		old.setSum(old.getSum() + other.getSum());
		old.setSum2(old.getSum2() + other.getSum2());
		old.setLine95Value(0);

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
			old.setAvg(old.getSum() / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	public void setMaxItems(int item) {
		m_maxNameLimit = item;
	}

	@Override
	public void visitMachine(Machine machine) {
		final Map<String, TransactionType> types = machine.getTypes();
		int size = types.size();

		if (size > m_maxTypeLimit) {
			Cat.logEvent("TooManyTransactionType", m_domain);
			List<TransactionType> all = new ArrayList<TransactionType>(types.values());
			Collections.sort(all, new TransactionTypeComparator());

			machine.getTypes().clear();

			for (int i = 0; i < m_maxTypeLimit; i++) {
				machine.addType(all.get(i));
			}

			TransactionType other = machine.findOrCreateType(CatConstants.OTHERS);

			for (int i = m_maxTypeLimit; i < size; i++) {
				mergeType(other, all.get(i));
			}
		}

		super.visitMachine(machine);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_domain = transactionReport.getDomain();
		super.visitTransactionReport(transactionReport);
	}

	;

	@Override
	public void visitType(TransactionType type) {
		Map<String, TransactionName> transactionNames = type.getNames();
		Set<String> names = transactionNames.keySet();
		Set<String> invalidates = new HashSet<String>();

		for (String temp : names) {
			int length = temp.length();

			if (length > m_lengthLimit) {
				invalidates.add(temp);
			} else {
				for (int i = 0; i < length; i++) {
					// invalidate char
					if (temp.charAt(i) > 126 || temp.charAt(i) < 32) {
						invalidates.add(temp);
						break;
					}
				}
			}
		}

		for (String name : invalidates) {
			TransactionName transactionName = transactionNames.remove(name);
			String validString = InvidStringBuilder.getValidString(name);

			transactionName.setId(validString);
			transactionNames.put(transactionName.getId(), transactionName);
		}

		int size = transactionNames.size();

		if (size > m_maxNameLimit) {
			Cat.logEvent("TooManyTransactionName", m_domain + ":" + type.getId());
			List<TransactionName> all = new ArrayList<TransactionName>(transactionNames.values());

			Collections.sort(all, new TransactionNameComparator());
			type.getNames().clear();

			for (int i = 0; i < m_maxNameLimit; i++) {
				type.addName(all.get(i));
			}

			TransactionName other = type.findOrCreateName(CatConstants.OTHERS);

			for (int i = m_maxNameLimit; i < size; i++) {
				mergeName(other, all.get(i));
			}
		}
		super.visitType(type);
	}

	private static class TransactionNameComparator implements Comparator<TransactionName> {
		@Override
		public int compare(TransactionName o1, TransactionName o2) {
			return Long.compare(o2.getTotalCount(), o1.getTotalCount());
		}
	}

	private static class TransactionTypeComparator implements Comparator<TransactionType> {
		@Override
		public int compare(TransactionType o1, TransactionType o2) {
			return Long.compare(o2.getTotalCount(), o1.getTotalCount());
		}
	}

}