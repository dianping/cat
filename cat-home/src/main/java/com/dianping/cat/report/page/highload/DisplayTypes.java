package com.dianping.cat.report.page.highload;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.report.task.highload.TransactionHighLoadUpdater.HighLoadReport;

public class DisplayTypes {

	public Map<String, List<HighLoadReport>> display(String sortBy, Map<String, List<HighLoadReport>> report) {
		if (!StringUtil.isEmpty(sortBy)) {
			for (List<HighLoadReport> reportsByType : report.values()) {
				Collections.sort(reportsByType, new HighloadReportComparator(sortBy));
			}
		}
		return report;
	}

	public class HighloadReportComparator implements Comparator<HighLoadReport> {

		private String m_sortBy;

		public HighloadReportComparator(String sortBy) {
			m_sortBy = sortBy;
		}

		@Override
		public int compare(HighLoadReport o1, HighLoadReport o2) {
			TransactionName n1 = o1.getName();
			TransactionName n2 = o2.getName();

			if (m_sortBy.equals("domain")) {
				return o2.getDomain().compareTo(o1.getDomain());
			}
			if (m_sortBy.equals("name")) {
				return n2.getId().compareTo(n1.getId());
			}
			if (m_sortBy.equals("total")) {
				long value = n2.getTotalCount() - n1.getTotalCount();

				if (value > 0) {
					return 1;
				} else if (value < 0) {
					return -1;
				} else {
					return 0;
				}
			}
			if (m_sortBy.equals("error")) {
				return (int) (n2.getFailCount() - n1.getFailCount());
			}
			if (m_sortBy.equals("failure")) {
				return (int) (n2.getFailPercent() * 100 - n1.getFailPercent() * 100);
			}
			if (m_sortBy.equals("min")) {
				return (int) (n2.getMin() * 100 - n1.getMin() * 100);
			}
			if (m_sortBy.equals("max")) {
				return (int) (n2.getMax() * 100 - n1.getMax() * 100);
			}
			if (m_sortBy.equals("avg")) {
				return (int) (n2.getAvg() * 100 - n1.getAvg() * 100);
			}
			if (m_sortBy.equals("95line")) {
				return (int) (n2.getLine95Value() * 100 - n1.getLine95Value() * 100);
			}
			if (m_sortBy.equals("999line")) {
				return (int) (n2.getLine99Value() * 100 - n1.getLine99Value() * 100);
			}
			if (m_sortBy.equals("std")) {
				return (int) (n2.getStd() * 100 - n1.getStd() * 100);
			}
			if (m_sortBy.equals("qps")) {
				return (int) (n2.getTps() * 100 - n1.getTps() * 100);
			}
			return 0;
		}

	}
}
