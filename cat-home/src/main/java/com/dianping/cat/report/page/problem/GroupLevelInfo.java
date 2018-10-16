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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.report.view.ProblemReportHelper;

public class GroupLevelInfo {
	private List<String> m_datas = new ArrayList<String>();

	private Map<String, GroupStatistics> m_groupStatistics = new LinkedHashMap<String, GroupStatistics>();

	private int m_minutes;

	private Model m_model;

	public GroupLevelInfo(Model model) {
		m_model = model;
		m_minutes = model.getLastMinute();
	}

	public GroupLevelInfo display(ProblemReport report) {
		Machine machine = report.getMachines().get(m_model.getIpAddress());
		if (machine == null) {
			return null;
		}
		Collection<Entity> entities = machine.getEntities().values();

		for (Entity entity : entities) {
			Map<String, JavaThread> threads = entity.getThreads();

			for (java.util.Map.Entry<String, JavaThread> entry : threads.entrySet()) {
				JavaThread thread = entry.getValue();

				String groupName = thread.getGroupName();
				GroupStatistics statistics = findOrCreatGroupStatistics(groupName, m_minutes);
				statistics.add(thread.getSegments(), entity.getType());
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

	public List<String> getGroups() {
		return SortHelper.sortDomain(m_groupStatistics.keySet());
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

		for (String group : getGroups()) {
			sb.append("<td>");
			params.put("group", group);
			GroupStatistics value = m_groupStatistics.get(group);
			for (String temp : value.getStatistics().get(minute)) {
				String url = ProblemReportHelper.creatLinkString(baseUrl, temp, params, "");
				sb.append(url);
			}
			sb.append("</td>");
		}
		return sb.toString();
	}

	public static class GroupStatistics {

		private Map<Integer, TreeSet<String>> m_statistics = new LinkedHashMap<Integer, TreeSet<String>>();

		public GroupStatistics(int lastMinute) {
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

		public TreeSet<String> getTag(int minutes) {
			return m_statistics.get(minutes);
		}
	}
}
