package com.dianping.cat.report.page.app.display;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.entity.Command;

public class AppReportSorter {

	private String m_sortBy;

	private AppReport m_report;

	private boolean m_sortValue = true;

	public AppReportSorter(AppReport report, String type) {
		m_report = report;
		m_sortBy = type;

		if ("domain".equals(type) || "command".equals(type) || StringUtils.isEmpty(type)) {
			m_sortValue = false;
		}
	}

	public AppReport getSortedReport() {
		Map<String, Command> commands = m_report.getCommands();
		List<Entry<String, Command>> tmp = new LinkedList<Entry<String, Command>>(commands.entrySet());
		Map<String, Command> results = new LinkedHashMap<String, Command>();

		Collections.sort(tmp, new AppComparator());

		for (Entry<String, Command> command : tmp) {
			results.put(command.getKey(), command.getValue());
		}
		m_report.getCommands().clear();
		m_report.getCommands().putAll(results);
		return m_report;

	}

	public class AppComparator implements Comparator<Entry<String, Command>> {

		@Override
		public int compare(Entry<String, Command> o1, Entry<String, Command> o2) {
			Command command1 = o1.getValue();
			Command command2 = o2.getValue();
			String domain1 = command1.getDomain();
			String domain2 = command2.getDomain();

			if (m_sortValue) {
				if (Constants.ALL.equals(domain1)) {
					return -1;
				} else if (Constants.ALL.equals(domain2)) {
					return 1;
				} else {
					Code code1 = command1.findOrCreateCode(m_sortBy);
					Code code2 = command2.findOrCreateCode(m_sortBy);

					return sortValue(code1, code2);
				}
			} else {
				if ("command".equals(m_sortBy)) {
					domain1 = command1.getTitle();
					if (StringUtils.isEmpty(domain1)) {
						domain1 = command1.getId();
					}

					domain2 = command2.getTitle();
					if (StringUtils.isEmpty(domain2)) {
						domain2 = command2.getId();
					}
				}
				return sortDomain(domain1, domain2);
			}
		}

		private int sortDomain(String o1, String o2) {
			if (Constants.ALL.equals(o1)) {
				return -1;
			}
			if (Constants.ALL.equals(o2)) {
				return 1;
			}
			boolean o1Empty = StringUtils.isEmpty(o1);
			boolean o2Empty = StringUtils.isEmpty(o2);

			if (o1Empty && o2Empty) {
				return 0;
			} else if (o1Empty) {
				return 1;
			} else if (o2Empty) {
				return -1;
			}
			return o1.compareTo(o2);
		}

		private int sortValue(Code c1, Code c2) {
			if (c1 == null && c2 == null) {
				return 0;
			} else if (c1 == null) {
				return 1;
			} else if (c2 == null) {
				return -1;
			} else {
				long count1 = c1.getCount();
				long count2 = c2.getCount();

				return count2 > count1 ? 1 : (count2 < count1 ? -1 : 0);
			}
		}
	}

}
