package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.site.lookup.util.StringUtils;

public class DisplayTransactionReport {

	private List<TransactionModel> m_results = new ArrayList<TransactionModel>();

	public List<TransactionModel> getResults() {
		return m_results;
	}

	public DisplayTransactionReport display(String sorted, TransactionReport report) {
		Map<String, TransactionType> types = report.getTypes();
		if (types != null) {
			for (Entry<String, TransactionType> entry : types.entrySet()) {
				m_results.add(new TransactionModel(entry.getKey(), entry.getValue()));
			}
		}
		if (!StringUtils.isEmpty(sorted)) {
			Collections.sort(m_results, new TransactionComparator(sorted));
		}
		return this;
	}

	public static class TransactionModel {
		private String m_type;

		private TransactionType m_detail;

		public TransactionModel(String str, TransactionType detail) {
			m_type = str;
			m_detail = detail;
		}

		public String getType() {
			return m_type;
		}

		public TransactionType getDetail() {
			return m_detail;
		}
	}

	public static class TransactionComparator implements Comparator<TransactionModel> {

		private String m_sorted;

		public TransactionComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(TransactionModel m1, TransactionModel m2) {
			if (m_sorted.equals("name") || m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				return (int) (m2.getDetail().getTotalCount() - m1.getDetail().getTotalCount());
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
			return 0;
		}
	}
}
