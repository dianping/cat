package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class DisplayTransactionTypeReport {

	private List<TransactionTypeModel> m_results = new ArrayList<TransactionTypeModel>();

	public DisplayTransactionTypeReport() {
	}

	public List<TransactionTypeModel> getResults() {
		return m_results;
	}

	public DisplayTransactionTypeReport display(String sorted, String ip, TransactionReport report) {
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
			return this;
		}
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

	public static class TransactionTypeModel {
		private String m_type;

		private TransactionType m_detail;

		public TransactionTypeModel() {
		}

		public TransactionTypeModel(String str, TransactionType detail) {
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
