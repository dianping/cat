package com.dianping.cat.report.page.highload;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.home.highload.entity.Name;
import com.dianping.cat.home.highload.entity.Type;

public class DisplayTypes {

	public HighloadReport display(String sortBy, HighloadReport report) {
		if (!StringUtil.isEmpty(sortBy)) {
			for (Type type : report.getTypes()) {
				List<Name> names = type.getNames();
				Collections.sort(names, new NameComparator(sortBy));
			}
		}
		return report;
	}

	public class NameComparator implements Comparator<Name> {

		private String m_sortBy;

		public NameComparator(String sortBy) {
			m_sortBy = sortBy;
		}

		@Override
		public int compare(Name n1, Name n2) {
			if (m_sortBy.equals("domain")) {
				return n2.getDomain().compareTo(n1.getDomain());
			}
			if (m_sortBy.equals("bu")) {
				return n2.getBu().compareTo(n1.getBu());
			}
			if (m_sortBy.equals("productline")) {
				return n2.getProductLine().compareTo(n1.getProductLine());
			}
			if (m_sortBy.equals("name")) {
				return n2.getId().compareTo(n1.getId());
			}
			if (m_sortBy.equals("total")) {
				long value = n2.getTotalCount() - n1.getTotalCount();

				return (int) value;
			}
			if (m_sortBy.equals("error")) {
				return (int) (n2.getFailCount() - n1.getFailCount());
			}
			if (m_sortBy.equals("failure")) {
				return (int) (n2.getFailPercent() * 100 - n1.getFailPercent() * 100);
			}
			if (m_sortBy.equals("avg")) {
				return (int) (n2.getAvg() * 100 - n1.getAvg() * 100);
			}
			if (m_sortBy.equals("95line")) {
				return (int) (n2.getLine95Value() * 100 - n1.getLine95Value() * 100);
			}
			if (m_sortBy.equals("qps")) {
				return (int) (n2.getTps() * 100 - n1.getTps() * 100);
			}
			return 0;
		}

	}
}
