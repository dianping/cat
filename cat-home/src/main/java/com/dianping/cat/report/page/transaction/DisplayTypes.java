package com.dianping.cat.report.page.transaction;

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

	public DisplayTypes() {
	}

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
			return m_type;
		}
	}
}
