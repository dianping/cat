package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.report.view.ProblemReportHelper;

public class ThreadLevelInfo {
	private int m_minutes;

	private String m_groupName;

	private Model m_model;

	private Map<String, GroupStatistics> m_groupStatistics = new LinkedHashMap<String, GroupStatistics>();

	private Map<String, TreeSet<String>> m_threadsInfo = new HashMap<String, TreeSet<String>>();

	private List<String> m_datas = new ArrayList<String>();

	public ThreadLevelInfo(Model model, String groupName) {
		m_groupName = groupName;
		m_model = model;
		m_minutes = model.getLastMinute();
	}

	public List<GroupDisplayInfo> getGroups() {
		List<GroupDisplayInfo> result = new ArrayList<GroupDisplayInfo>();

		for (java.util.Map.Entry<String, TreeSet<String>> entry : m_threadsInfo.entrySet()) {
			result.add(new GroupDisplayInfo().setName(entry.getKey()).setNumber(entry.getValue().size()));
		}
		Collections.sort(result, new Comparator<GroupDisplayInfo>() {
			@Override
			public int compare(GroupDisplayInfo o1, GroupDisplayInfo o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return result;
	}

	public List<String> getThreads() {
		List<String> result = new ArrayList<String>();

		for (TreeSet<String> set : m_threadsInfo.values()) {
			for (String thread : set) {
				result.add(thread);
			}
		}
		return result;
	}

	private TreeSet<String> getThreadsByGroup(String groupName) {
		return m_threadsInfo.get(groupName);
	}

	private String getShowDetailByMinte(int minute) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String baseUrl = "/cat/r/p?op=detail";
		params.put("domain", m_model.getDomain());
		params.put("ip", m_model.getIpAddress());
		params.put("date", m_model.getDate());
		params.put("minute", Integer.toString(minute));

		StringBuilder sb = new StringBuilder().append("<td>");
		String minuteStr = Integer.toString(minute);
		if (minute < 10) {
			minuteStr = "0" + minute;
		}
		sb.append(ProblemReportHelper.creatLinkString(baseUrl, "minute", params, minuteStr));
		sb.append("</td>");

		for (GroupDisplayInfo group : getGroups()) {
			GroupStatistics value = m_groupStatistics.get(group);
			String groupName = group.getName();
			Set<String> threads = getThreadsByGroup(groupName);
			Map<String, TheadStatistics> temps = value.getStatistics();

			for (String thread : threads) {
				TheadStatistics theadStatistics = temps.get(thread);
				HashSet<String> errors = theadStatistics.getStatistics().get(minute);
				sb.append("<td>");
				for (String error : errors) {
					params.put("group", groupName);
					if (groupName.equals(m_groupName)) {
						params.put("thread", thread);
					}
					String url = ProblemReportHelper.creatLinkString(baseUrl, error, params, "");
					sb.append(url);
				}
				sb.append("</td>");
			}
		}
		return sb.toString();
	}

	private void findOrCreatThreadInfo(String groupName, String threadName) {
		TreeSet<String> sets = m_threadsInfo.get(groupName);

		if (sets != null) {
			sets.add(threadName);
		} else {
			sets = new TreeSet<String>();

			sets.add(threadName);
			m_threadsInfo.put(groupName, sets);
		}
	}

	public ThreadLevelInfo display(ProblemReport report) {
		Machine machine = report.getMachines().get(m_model.getIpAddress());
		if (machine == null) {
			return null;
		}
		Map<String, JavaThread> threads = machine.getThreads();

		for (java.util.Map.Entry<String, JavaThread> entry : threads.entrySet()) {
			JavaThread thread = entry.getValue();
			String groupName = thread.getGroupName();
			String threadId = thread.getId();
			GroupStatistics statistics = findOrCreatGroupStatistics(groupName, m_minutes);

			if (groupName.equals(m_groupName)) {
				statistics.add(threadId, thread.getSegments(), m_minutes);
				findOrCreatThreadInfo(groupName, threadId);
			} else {
				statistics.add(groupName, thread.getSegments(), m_minutes);
				findOrCreatThreadInfo(groupName, groupName);
			}
		}
		long currentTimeMillis = System.currentTimeMillis();
		long currentHours = currentTimeMillis - currentTimeMillis % (60 * 60 * 1000);
		if (currentHours == m_model.getLongDate()) {
			for (int i = m_minutes; i >= 0; i--) {
				m_datas.add(getShowDetailByMinte(i));
			}
		} else {
			for (int i = 0; i <= m_minutes; i++) {
				m_datas.add(getShowDetailByMinte(i));
			}
		}
		return this;
	}

	public GroupStatistics findOrCreatGroupStatistics(String groupName, int lastMinute) {
		m_minutes = lastMinute;

		GroupStatistics value = m_groupStatistics.get(groupName);
		if (value == null) {
			GroupStatistics result = new GroupStatistics();
			m_groupStatistics.put(groupName, result);
			return result;
		} else {
			return value;
		}
	}

	public List<String> getDatas() {
		return m_datas;
	}

	public static class GroupDisplayInfo {
		private String m_name;

		private int m_number;

		public String getName() {
			return m_name;
		}

		public GroupDisplayInfo setName(String name) {
			m_name = name;
			return this;
		}

		public int getNumber() {
			return m_number;
		}

		public GroupDisplayInfo setNumber(int number) {
			m_number = number;
			return this;
		}

	}

	public static class GroupStatistics {

		private Map<String, TheadStatistics> m_statistics = new LinkedHashMap<String, TheadStatistics>();

		public void add(String threadId, Map<Integer, Segment> segments, int minute) {
			findOrCreatTheadStatistics(threadId, minute).add(segments);
		}

		public Map<String, TheadStatistics> getStatistics() {
			return m_statistics;
		}

		public void setStatistics(Map<String, TheadStatistics> statistics) {
			m_statistics = statistics;
		}

		public TheadStatistics findOrCreatTheadStatistics(String threadName, int minute) {
			TheadStatistics statistics = m_statistics.get(threadName);
			if (statistics == null) {
				TheadStatistics result = new TheadStatistics(minute);

				m_statistics.put(threadName, result);
				return result;
			} else {
				return statistics;
			}
		}
	}

	public static class TheadStatistics {

		private Map<Integer, HashSet<String>> m_statistics = new LinkedHashMap<Integer, HashSet<String>>();

		public TheadStatistics(int lastMinute) {
			for (int i = 0; i <= lastMinute; i++) {
				m_statistics.put(new Integer(i), new HashSet<String>());
			}
		}

		public void add(Map<Integer, Segment> segments) {
			for (java.util.Map.Entry<Integer, Segment> entry : segments.entrySet()) {
				List<Entry> entries = entry.getValue().getEntries();
				for (Entry temp : entries) {

					m_statistics.get(entry.getKey()).add(temp.getType());
				}
			}
		}

		public Map<Integer, HashSet<String>> getStatistics() {
			return m_statistics;
		}
	}
}
