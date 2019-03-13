/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.report.view.ProblemReportHelper;

public class ThreadLevelInfo {
	private List<String> m_datas = new ArrayList<String>();

	private String m_groupName;

	private Map<String, GroupStatistics> m_groupStatistics = new LinkedHashMap<String, GroupStatistics>();

	private int m_minutes;

	private Model m_model;

	private Map<String, TreeSet<String>> m_threadsInfo = new HashMap<String, TreeSet<String>>();

	public ThreadLevelInfo(Model model, String groupName) {
		m_groupName = groupName;
		m_model = model;
		m_minutes = model.getLastMinute();
	}

	public ThreadLevelInfo display(ProblemReport report) {
		Machine machine = report.getMachines().get(m_model.getIpAddress());
		if (machine == null) {
			return null;
		}

		Collection<Entity> entities = machine.getEntities().values();
		for (Entity temp : entities) {
			Map<String, JavaThread> threads = temp.getThreads();

			for (java.util.Map.Entry<String, JavaThread> entry : threads.entrySet()) {
				JavaThread thread = entry.getValue();
				String groupName = thread.getGroupName();
				String threadId = thread.getId();
				GroupStatistics statistics = findOrCreatGroupStatistics(groupName, m_minutes);

				if (groupName.equals(m_groupName)) {
					statistics.add(threadId, thread.getSegments(), m_minutes, temp.getType());
					findOrCreatThreadInfo(groupName, threadId);
				} else {
					statistics.add(groupName, thread.getSegments(), m_minutes, temp.getType());
					findOrCreatThreadInfo(groupName, groupName);
				}
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

	public List<String> getDatas() {
		return m_datas;
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

	private String getShowDetailByMinte(int minute) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		String baseUrl = "/cat/r/p?op=detail";
		params.put("domain", m_model.getDomain());
		params.put("ip", m_model.getIpAddress());
		params.put("date", m_model.getDate());
		params.put("minute", Integer.toString(minute));

		StringBuilder sb = new StringBuilder().append("<td>");
		String minuteStr = m_model.getDisplayHour() + ":";
		if (minute < 10) {
			minuteStr = minuteStr + "0" + Integer.toString(minute);
		} else {
			minuteStr = minuteStr + Integer.toString(minute);
		}
		sb.append(ProblemReportHelper.creatLinkString(baseUrl, "minute", params, minuteStr));
		sb.append("</td>");

		for (GroupDisplayInfo group : getGroups()) {
			String groupName = group.getName();
			GroupStatistics value = m_groupStatistics.get(groupName);
			Set<String> threads = getThreadsByGroup(groupName);
			Map<String, TheadStatistics> temps = value.getStatistics();

			for (String thread : threads) {
				TheadStatistics theadStatistics = temps.get(thread);
				TreeSet<String> errors = theadStatistics.getStatistics().get(minute);
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

		public void add(String threadId, Map<Integer, Segment> segments, int minute, String type) {
			findOrCreatTheadStatistics(threadId, minute).add(segments, type);
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

		public Map<String, TheadStatistics> getStatistics() {
			return m_statistics;
		}

		public void setStatistics(Map<String, TheadStatistics> statistics) {
			m_statistics = statistics;
		}
	}

	public static class TheadStatistics {

		private Map<Integer, TreeSet<String>> m_statistics = new LinkedHashMap<Integer, TreeSet<String>>();

		public TheadStatistics(int lastMinute) {
			for (int i = 0; i <= lastMinute; i++) {
				m_statistics.put(i, new TreeSet<String>());
			}
		}

		public void add(Map<Integer, Segment> segments, String type) {
			for (java.util.Map.Entry<Integer, Segment> entry : segments.entrySet()) {
				findOrCreat(entry.getKey()).add(type);
			}
		}

		public TreeSet<String> findOrCreat(Integer key) {
			TreeSet<String> result = m_statistics.get(key);
			if (result == null) {
				result = new TreeSet<String>();
				m_statistics.put(key, result);
			}
			return result;
		}

		public Map<Integer, TreeSet<String>> getStatistics() {
			return m_statistics;
		}
	}
}
