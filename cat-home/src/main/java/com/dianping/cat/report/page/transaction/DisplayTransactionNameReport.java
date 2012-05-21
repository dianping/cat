package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DisplayTransactionNameReport {

	private List<TransactionNameModel> m_results = new ArrayList<TransactionNameModel>();

	public DisplayTransactionNameReport() {

	}

	public List<TransactionNameModel> getResults() {
		return m_results;
	}

	public DisplayTransactionNameReport display(String sorted, String type, String ip,TransactionReport report, String queryName) {
		Map<String, TransactionType> types = report.getMachines().get(ip).getTypes();
		if (types != null) {
			TransactionType names = types.get(type);

			if (names != null) {
				for (Entry<String, TransactionName> entry : names.getNames().entrySet()) {
					String transTypeName = entry.getValue().getId();
					boolean isAdd = (queryName == null || queryName.length() == 0 || transTypeName.toLowerCase().contains(
					      queryName.trim().toLowerCase()));
					if (isAdd) {
						m_results.add(new TransactionNameModel(entry.getKey(), entry.getValue()));
					}
				}
			}
		}
		if (sorted == null) {
			sorted = "avg";
		}
		Collections.sort(m_results, new TransactionNameComparator(sorted));
		return this;
	}

	public static class TransactionNameModel {
		private String m_type;

		private TransactionName m_detail;

		public TransactionNameModel() {
		}

		public TransactionNameModel(String str, TransactionName detail) {
			m_type = str;
			m_detail = detail;
		}

		public String getType() {
			return m_type;
		}

		public TransactionName getDetail() {
			return m_detail;
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
			if (m_sorted.equals("95line")) {
				return (int) (m2.getDetail().getLine95Value() * 100 - m1.getDetail().getLine95Value() * 100);
			}
			return 0;
		}
	}
}
