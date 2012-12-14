package com.dianping.cat.consumer.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class TransactionReportUrlFilter extends com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder
      implements LogEnabled {

	private int m_maxItems = 200;

	private Logger m_logger;

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
	public void visitTransactionReport(TransactionReport transactionReport) {
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		if ("URL".equals(type.getId())) {
			Map<String, TransactionName> transactionNames = type.getNames();

			Set<String> names = transactionNames.keySet();
			Set<String> invalidates = new HashSet<String>();

			for (String temp : names) {
				int length = temp.length();

				for (int i = 0; i < length; i++) {
					if (temp.charAt(i) > 255 || temp.charAt(i) < 0) {
						invalidates.add(temp);
						continue;
					}
				}
			}

			for (String name : invalidates) {
				m_logger.error("remove invalidate url " + name);
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
		}
		super.visitType(type);
	}

	public static class TransactionNameCompator implements Comparator<TransactionName> {

		@Override
		public int compare(TransactionName o1, TransactionName o2) {
			return (int) (o2.getTotalCount() - o1.getTotalCount());
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}