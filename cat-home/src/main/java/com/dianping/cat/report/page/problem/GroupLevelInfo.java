package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.report.view.ProblemReportHelper;

public class GroupLevelInfo {
	private int m_minutes;

	private Model m_model;

	private Map<String, GroupStatistics> m_groupStatistics = new LinkedHashMap<String, GroupStatistics>();

	private List<String> m_datas = new ArrayList<String>();

	public GroupLevelInfo(Model model) {
		m_model = model;
		m_minutes = model.getLastMinute();
	}

	public Set<String> getGroups() {
		return m_groupStatistics.keySet();
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
			minuteStr ="0" +minute;
		}
		sb.append(ProblemReportHelper.creatLinkString(baseUrl, "minute", params, minuteStr));
		sb.append("</td>");
		
		for (java.util.Map.Entry<String, GroupStatistics> statistics : m_groupStatistics.entrySet()) {
			sb.append("<td>");
			params.put("group", statistics.getKey());
			GroupStatistics value = statistics.getValue();
			for (String temp : value.getStatistics().get(minute)) {
				String url = ProblemReportHelper.creatLinkString(baseUrl, temp, params,"");
				sb.append(url);
			}
			sb.append("</td>");
		}
		return sb.toString();
	}

	public GroupLevelInfo display(ProblemReport report) {
		Machine machine = report.getMachines().get(m_model.getIpAddress());
		if (machine == null) {
			return null;
		}
		Map<String, JavaThread> threads = machine.getThreads();
		for (java.util.Map.Entry<String, JavaThread> entry : threads.entrySet()) {
			JavaThread thread = entry.getValue();

			String groupName = thread.getGroupName();
			GroupStatistics statistics = findOrCreatGroupStatistics(groupName, m_minutes);
			statistics.add(thread.getSegments());
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
			GroupStatistics result = new GroupStatistics(lastMinute);

			m_groupStatistics.put(groupName, result);
			return result;
		} else {
			return value;
		}
	}

	public List<String> getDatas() {
		return m_datas;
	}

	public static class GroupStatistics {

		private Map<Integer, HashSet<String>> m_statistics = new LinkedHashMap<Integer, HashSet<String>>();

		public HashSet<String> getTag(int minutes) {
			return m_statistics.get(minutes);
		}

		public GroupStatistics(int lastMinute) {
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
