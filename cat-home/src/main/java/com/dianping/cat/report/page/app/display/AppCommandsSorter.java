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

public class AppCommandsSorter {

	private String m_sortBy;

	private DisplayCommands m_commands;

	private boolean m_sortValue = true;

	public static final String DOMAIN = "domain";

	public static final String COMMAND = "command";

	public static final String BU = "bu";

	public static final String DEPARTMENT = "department";

	public static final String COUNT = "count";

	public static final String TRANSACTION_COUNT = "transactionCount";

	public static final String AVG = "avg";

	public static final String TRANSACTION_AVG = "transactionAvg";

	public static final String COUNT_COMPARISON = "countComparison";

	public static final String AVG_COMPARISON = "avgComparison";

	public static final String REQUEST = "request";

	public static final String RESPONSE = "response";

	public static final String SUCCESS = "success";

	public AppCommandsSorter(DisplayCommands commands, String type) {
		m_commands = commands;
		m_sortBy = type;

		if (DOMAIN.equals(type) || COMMAND.equals(type) || BU.equals(type) || DEPARTMENT.equals(type)
		      || StringUtils.isEmpty(type)) {
			m_sortValue = false;
		}
	}

	public DisplayCommands getSortedCommands() {
		Map<Integer, DisplayCommand> commands = m_commands.getCommands();
		List<Entry<Integer, DisplayCommand>> tmp = new LinkedList<Entry<Integer, DisplayCommand>>(commands.entrySet());
		Map<Integer, DisplayCommand> results = new LinkedHashMap<Integer, DisplayCommand>();

		Collections.sort(tmp, new AppComparator());

		for (Entry<Integer, DisplayCommand> command : tmp) {
			results.put(command.getKey(), command.getValue());
		}
		m_commands.getCommands().clear();
		m_commands.getCommands().putAll(results);
		return m_commands;
	}

	public class AppComparator implements Comparator<Entry<Integer, DisplayCommand>> {

		@Override
		public int compare(Entry<Integer, DisplayCommand> o1, Entry<Integer, DisplayCommand> o2) {
			DisplayCommand command1 = o1.getValue();
			DisplayCommand command2 = o2.getValue();
			String str1 = command1.getDomain();
			String str2 = command2.getDomain();

			if (m_sortValue) {
				if (Constants.ALL.equals(str1)) {
					return -1;
				} else if (Constants.ALL.equals(str2)) {
					return 1;
				} else {
					return sortValue(command1, command2);
				}
			} else {
				return sortStr(command1, command2);
			}
		}

		private int sortCount(DisplayCode c1, DisplayCode c2) {
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

		private int sortStr(DisplayCommand command1, DisplayCommand command2) {
			String str1 = command1.getDomain();
			String str2 = command2.getDomain();

			if (COMMAND.equals(m_sortBy)) {
				str1 = command1.getTitle();

				if (StringUtils.isEmpty(str1)) {
					str1 = command1.getName();
				}

				str2 = command2.getTitle();
				if (StringUtils.isEmpty(str2)) {
					str2 = command2.getName();
				}
			} else if (BU.equals(m_sortBy) || DEPARTMENT.equals(m_sortBy)) {
				str1 = command1.getDepartment();
				str2 = command2.getDepartment();
				String bu1 = command1.getBu();
				String bu2 = command2.getBu();
				int ret = sortStr(bu1, bu2);

				if (ret == 0) {
					return sortStr(str1, str2);
				}
			}
			return sortStr(str1, str2);
		}

		private int sortStr(String o1, String o2) {
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

		private int sortValue(DisplayCommand command1, DisplayCommand command2) {
			if (COUNT.equals(m_sortBy)) {
				long count1 = command1.getCount();
				long count2 = command2.getCount();

				return count2 > count1 ? 1 : (count2 < count1 ? -1 : 0);
			} else if (AVG.equals(m_sortBy)) {
				double avg1 = command1.getAvg();
				double avg2 = command2.getAvg();

				return avg2 > avg1 ? 1 : (avg2 < avg1 ? -1 : 0);
			} else if (SUCCESS.equals(m_sortBy)) {
				double ratio1 = command1.getSuccessRatio();
				double ratio2 = command2.getSuccessRatio();

				return ratio2 > ratio1 ? 1 : (ratio2 < ratio1 ? -1 : 0);
			} else if (REQUEST.equals(m_sortBy)) {
				double avg1 = command1.getRequestAvg();
				double avg2 = command2.getRequestAvg();

				return avg2 > avg1 ? 1 : (avg2 < avg1 ? -1 : 0);
			} else if (RESPONSE.equals(m_sortBy)) {
				double avg1 = command1.getResponseAvg();
				double avg2 = command2.getResponseAvg();

				return avg2 > avg1 ? 1 : (avg2 < avg1 ? -1 : 0);
			} else if (TRANSACTION_COUNT.equals(m_sortBy)) {
				long count1 = command1.getTransactionCount();
				long count2 = command2.getTransactionCount();

				return count2 > count1 ? 1 : (count2 < count1 ? -1 : 0);
			} else if (TRANSACTION_AVG.equals(m_sortBy)) {
				double avg1 = command1.getTransactionAvg();
				double avg2 = command2.getTransactionAvg();

				return avg2 > avg1 ? 1 : (avg2 < avg1 ? -1 : 0);
			} else if ((COUNT_COMPARISON.equals(m_sortBy))) {
				double avg1 = command1.getCountComparison();
				double avg2 = command2.getCountComparison();

				return avg2 > avg1 ? -1 : (avg2 < avg1 ? 1 : 0);
			} else if ((AVG_COMPARISON.equals(m_sortBy))) {
				double avg1 = command1.getAvgComparison();
				double avg2 = command2.getAvgComparison();

				return avg2 > avg1 ? -1 : (avg2 < avg1 ? 1 : 0);
			} else {
				DisplayCode code1 = command1.findOrCreateCode(m_sortBy);
				DisplayCode code2 = command2.findOrCreateCode(m_sortBy);

				return sortCount(code1, code2);
			}
		}
	}
}
