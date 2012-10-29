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

public class DisplayNames {

	private List<TransactionNameModel> m_results = new ArrayList<TransactionNameModel>();

	public DisplayNames() {
	}

	public DisplayNames display(String sorted, String type, String ip, TransactionReport report, String queryName) {
		Map<String, TransactionType> types = report.getMachines().get(ip).getTypes();
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

		public String getType() {
			return m_type;
		}
	}
}
