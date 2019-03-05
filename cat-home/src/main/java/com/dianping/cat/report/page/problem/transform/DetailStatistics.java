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
package com.dianping.cat.report.page.problem.transform;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.SortHelper;

public class DetailStatistics extends BaseVisitor {

	private String m_groupName;

	private String m_ip = "";

	private int m_minute;

	private Map<String, TypeStatistics> m_status = new TreeMap<String, TypeStatistics>();

	private String m_threadId;

	public Map<String, TypeStatistics> getStatus() {
		return m_status;
	}

	public String getSubTitle() {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isEmpty(m_threadId) && StringUtils.isEmpty(m_groupName)) {
			return "All Thread Groups";
		} else if (!StringUtils.isEmpty(m_groupName) && StringUtils.isEmpty(m_threadId)) {
			return "All Threads in Group:" + m_groupName;
		} else if (!StringUtils.isEmpty(m_groupName) && !StringUtils.isEmpty(m_threadId)) {
			return "Thread :" + m_threadId;
		}
		return sb.toString();
	}

	public String getUrl() {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(m_groupName)) {
			sb.append("&group=").append(m_groupName);
		}
		if (!StringUtils.isEmpty(m_threadId)) {
			sb.append("&thread=").append(m_threadId);
		}
		return sb.toString();
	}

	private boolean isContents(String groupName, String threadId) {
		if (m_groupName != null && m_groupName.equals(groupName) == false) {
			return false;
		}
		if (m_threadId != null && m_threadId.equals(threadId) == false) {
			return false;
		}
		return true;
	}

	public DetailStatistics setGroupName(String groupName) {
		m_groupName = groupName;
		return this;
	}

	public DetailStatistics setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public DetailStatistics setMinute(int minute) {
		m_minute = minute;
		return this;
	}

	public DetailStatistics setThreadId(String threadId) {
		m_threadId = threadId;
		return this;
	}

	private void statisticsSegment(Segment segment, String type, String status) {
		TypeStatistics statusValue = m_status.get(type);

		if (statusValue == null) {
			statusValue = new TypeStatistics(type);
			m_status.put(type, statusValue);
		}
		statusValue.statics(status, segment);
	}

	@Override
	public void visitMachine(Machine machine) {
		if (machine.getIp().equals(m_ip)) {
			Collection<Entity> entities = machine.getEntities().values();

			for (Entity entity : entities) {
				Map<String, JavaThread> threads = entity.getThreads();

				for (JavaThread thread : threads.values()) {
					String threadId = thread.getId();
					String groupName = thread.getGroupName();

					if (isContents(groupName, threadId)) {
						Segment segment = thread.getSegments().get(m_minute);

						if (segment != null) {
							statisticsSegment(segment, entity.getType(), entity.getStatus());
						}
					}
				}
			}
		}
	}

	public static class StatusStatistics {
		private int m_count;

		private List<String> m_links = new ArrayList<String>();

		private String m_status;

		private StatusStatistics(String status) {
			m_status = status;
		}

		public String getEncodeStatus() {
			try {
				return URLEncoder.encode(m_status, "utf-8");
			} catch (Exception e) {
				return m_status;
			}
		}

		public void addLinks(String link) {
			m_links.add(link);
		}

		public int getCount() {
			return m_count;
		}

		public StatusStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public List<String> getLinks() {
			return m_links;
		}

		public StatusStatistics setLinks(List<String> links) {
			m_links = links;
			return this;
		}

		public String getStatus() {
			return m_status;
		}

		public StatusStatistics setStatus(String status) {
			m_status = status;
			return this;
		}

		public void statics(Segment segment) {
			m_count += segment.getCount();
			if (m_links.size() < 60) {
				m_links.addAll(segment.getMessages());
				if (m_links.size() > 60) {
					m_links = m_links.subList(0, 60);
				}
			}
		}
	}

	public static class TypeStatistics {
		private int m_count;

		private Map<String, StatusStatistics> m_status = new LinkedHashMap<String, StatusStatistics>();

		private String m_type;

		public TypeStatistics(String type) {
			m_type = type;
		}

		public int getCount() {
			return m_count;
		}

		public TypeStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public Map<String, StatusStatistics> getStatus() {
			Map<String, StatusStatistics> result = SortHelper
									.sortMap(m_status,	new Comparator<java.util.Map.Entry<String, StatusStatistics>>() {
										@Override
										public int compare(java.util.Map.Entry<String, StatusStatistics> o1,
																java.util.Map.Entry<String, StatusStatistics> o2) {
											return o2.getValue().getCount() - o1.getValue().getCount();
										}
									});
			return result;
		}

		public void setStatus(Map<String, StatusStatistics> status) {
			m_status = status;
		}

		public String getType() {
			return m_type;
		}

		public TypeStatistics setType(String type) {
			m_type = type;
			return this;
		}

		public void statics(String status, Segment segment) {
			m_count += segment.getCount();
			StatusStatistics value = m_status.get(status);
			if (value == null) {
				value = new StatusStatistics(status);
				m_status.put(status, value);
			}
			value.statics(segment);
		}
	}
}
