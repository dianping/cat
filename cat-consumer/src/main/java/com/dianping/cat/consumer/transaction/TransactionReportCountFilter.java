package com.dianping.cat.consumer.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class TransactionReportCountFilter extends com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
	private int m_maxItems = 400;

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

	@Override
	public void visitType(TransactionType type) {
		Map<String, TransactionName> transactionNames = type.getNames();
		Set<String> names = transactionNames.keySet();
		Set<String> invalidates = new HashSet<String>();

		for (String temp : names) {
			int length = temp.length();

			for (int i = 0; i < length; i++) {
				// invalidate char
				if (temp.charAt(i) > 126 || temp.charAt(i) < 33) {
					invalidates.add(temp);
					continue;
				}
			}
		}

		for (String name : invalidates) {
			transactionNames.remove(name);
		}

		int size = transactionNames.size();

		if (size > m_maxItems) {
			List<TransactionName> all = new ArrayList<TransactionName>(transactionNames.values());

			Collections.sort(all, new TransactionNameCompator());
			type.getNames().clear();

			for (int i = 0; i < m_maxItems; i++) {
				type.addName(all.get(i));
			}

			TransactionName other = type.findOrCreateName("OTHERS");

			for (int i = m_maxItems; i < size; i++) {
				mergeName(other, all.get(i));
			}
		}

		List<String> toRemove = new ArrayList<String>();
		TransactionName other = type.findOrCreateName("OTHERS");

		transactionNames = type.getNames();
		for (Entry<String, TransactionName> entry : type.getNames().entrySet()) {
			TransactionName tansactionName = entry.getValue();

			if (tansactionName.getTotalCount() == 1) {
				toRemove.add(entry.getKey());
				mergeName(other, tansactionName);
			}
		}

		for (String name : toRemove) {
			transactionNames.remove(name);
		}
		super.visitType(type);
	}

	private static class TransactionNameCompator implements Comparator<TransactionName> {
		@Override
		public int compare(TransactionName o1, TransactionName o2) {
			return (int) (o2.getTotalCount() - o1.getTotalCount());
		}
	}
}