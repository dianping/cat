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

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.report.page.problem.LongConfig;

public class ProblemStatistics extends BaseVisitor {

	private Map<String, TypeStatistics> m_status = new TreeMap<String, TypeStatistics>(new Comparator<String>() {

		@Override
		public int compare(String str1, String str2) {
			if (str1.equals(str2)) {
				return 0;
			}
			if ("error".equals(str1)) {
				return -1;
			}
			if ("error".equals(str2)) {
				return 1;
			}
			if ("heartbeat".equals(str1)) {
				return 1;
			}
			if ("heartbeat".equals(str2)) {
				return -1;
			}
			return str1.compareTo(str2);
		}
	});

	private boolean m_allIp = false;

	private String m_ip = "";

	private List<String> m_ips;

	private LongConfig m_longConfig = new LongConfig();

	private List<Duration> getDurationsByType(String type, Entity entity) {
		List<Duration> durations = new ArrayList<Duration>();
		if (ProblemType.LONG_URL.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entity.getDurations().entrySet()) {
				if (temp.getKey() >= m_longConfig.getUrlThreshold()) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_SQL.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entity.getDurations().entrySet()) {
				if (temp.getKey() >= m_longConfig.getSqlThreshold()) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_SERVICE.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entity.getDurations().entrySet()) {
				if (temp.getKey() >= m_longConfig.getServiceThreshold()) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_CALL.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entity.getDurations().entrySet()) {
				if (temp.getKey() >= m_longConfig.getCallThreshold()) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_CACHE.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entity.getDurations().entrySet()) {
				if (temp.getKey() >= m_longConfig.getCacheThreshold()) {
					durations.add(temp.getValue());
				}
			}
		} else {
			durations.add(entity.getDurations().get(0));
		}
		return durations;
	}

	public List<String> getIps() {
		return m_ips;
	}

	public void setIps(List<String> ips) {
		m_ips = ips;
	}

	public Map<String, TypeStatistics> getStatus() {
		return m_status;
	}

	public ProblemStatistics setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}

	public ProblemStatistics setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public ProblemStatistics setLongConfig(LongConfig config) {
		m_longConfig = config;
		return this;
	}

	private void statisticsDuration(Entity entity) {
		String type = entity.getType();
		String status = entity.getStatus();
		List<Duration> durations = getDurationsByType(type, entity);
		for (Duration duration : durations) {
			TypeStatistics statusValue = m_status.get(type);

			if (statusValue == null) {
				statusValue = new TypeStatistics(type);
				m_status.put(type, statusValue);
			}
			statusValue.statics(status, duration);
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		if (m_allIp == true || m_ip.equals(machine.getIp())) {
			Collection<Entity> entities = machine.getEntities().values();

			for (Entity entity : entities) {
				statisticsDuration(entity);
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

		public String getEncodeStatus() {
			try {
				return URLEncoder.encode(m_status, "utf-8");
			} catch (Exception e) {
				return m_status;
			}
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

		public void statics(Duration duration) {
			m_count += duration.getCount();
			if (m_links.size() < 60) {
				m_links.addAll(duration.getMessages());
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

		public void statics(String status, Duration duration) {
			if (duration != null) {
				m_count += duration.getCount();

				StatusStatistics value = m_status.get(status);
				if (value == null) {
					value = new StatusStatistics(status);
					m_status.put(status, value);
				}
				value.statics(duration);
			}
		}
	}
}
