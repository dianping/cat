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
package com.dianping.cat.report.page.heartbeat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;

public class HeartbeatSvgGraph {

	private static final String DAL = "dal";

	private static final Map<String, Integer> INDEX = new HashMap<String, Integer>();

	private static final AtomicInteger INDEX_COUNTER = new AtomicInteger(0);

	private transient HeartbeatDisplayPolicyManager m_manager;

	private GraphBuilder m_builder;

	private Map<String, Map<String, double[]>> m_extensions = new LinkedHashMap<String, Map<String, double[]>>();

	public HeartbeatSvgGraph(GraphBuilder builder, HeartbeatDisplayPolicyManager manager) {
		m_builder = builder;
		m_manager = manager;
	}

	private void addSortedGroups(Map<String, Map<String, double[]>> tmpExtensions) {
		List<String> orderedGroupNames = m_manager.sortGroupNames(m_extensions.keySet());

		for (String groupName : orderedGroupNames) {
			Map<String, double[]> extensionGroup = m_extensions.get(groupName);

			tmpExtensions.put(groupName, extensionGroup);
		}
	}

	private void buildExtensionGraph(Map<String, ExtensionGroup> graphs, Entry<String, Map<String, double[]>> entry) {
		String title = entry.getKey();

		if (title.equalsIgnoreCase(DAL)) {
			for (Entry<String, double[]> subEntry : entry.getValue().entrySet()) {
				String key = subEntry.getKey();
				int pos = key.lastIndexOf('-');

				if (pos > 0) {
					String db = "Dal " + key.substring(0, pos);
					String subTitle = key.substring(pos + 1);
					ExtensionGroup extensitonGroup = graphs.get(db);

					if (extensitonGroup == null) {
						extensitonGroup = new ExtensionGroup();

						graphs.put(db, extensitonGroup);
					}

					if (!INDEX.containsKey(subTitle)) {
						INDEX.put(subTitle, INDEX_COUNTER.getAndIncrement());
					}

					String svg = m_builder
											.build(new HeartbeatSvgBuilder(INDEX.get(subTitle), subTitle, "Minute", "Count",	subEntry.getValue()));
					extensitonGroup.getSvgs().put(subTitle, svg);
				}
			}
		} else {
			ExtensionGroup extensitonGroup = graphs.get(title);

			if (extensitonGroup == null) {
				extensitonGroup = new ExtensionGroup();
				graphs.put(title, extensitonGroup);
			}

			int i = 0;
			for (Entry<String, double[]> item : entry.getValue().entrySet()) {
				String key = item.getKey();
				Metric metricConfig = m_manager.queryMetric(title, key);
				String svgTitle = key;
				String lable = "";

				if (metricConfig != null) {
					String configTitle = metricConfig.getTitle();

					if (configTitle != null) {
						svgTitle = configTitle;
					}
					lable = metricConfig.getLable();
				}
				String svg = m_builder.build(new HeartbeatSvgBuilder(i++, svgTitle, "Minute", lable, item.getValue()));
				extensitonGroup.getSvgs().put(key, svg);
			}
		}
	}

	private Map<String, Map<String, double[]>> dealWithExtensions() {
		Map<String, Map<String, double[]>> result = new LinkedHashMap<String, Map<String, double[]>>();

		addSortedGroups(result);
		for (Entry<String, Map<String, double[]>> entry : result.entrySet()) {
			String groupName = entry.getKey();
			Map<String, double[]> originMetrics = entry.getValue();
			List<String> metricNames = m_manager.sortMetricNames(groupName, originMetrics.keySet());
			Map<String, double[]> normalizedMetrics = new LinkedHashMap<String, double[]>();

			for (String metricName : metricNames) {
				double[] values = originMetrics.get(metricName);

				if (m_manager.isDelta(groupName, metricName)) {
					values = getAddedCount(values);
				}

				int unit = m_manager.queryUnit(groupName, metricName);

				for (int i = 0; i <= 59; i++) {
					values[i] = values[i] / unit;
				}
				normalizedMetrics.put(metricName, values);
			}
			entry.setValue(normalizedMetrics);
		}
		return result;
	}

	public HeartbeatSvgGraph display(HeartbeatReport report, String ip) {
		if (report == null) {
			return this;
		}
		Machine machine = report.getMachines().get(ip);

		if (machine == null) {
			return this;
		}

		List<Period> periods = machine.getPeriods();
		int size = periods.size();

		for (; size > 0; size--) {
			Period period = periods.get(size - 1);
			int minute = period.getMinute();

			for (Entry<String, Extension> entry : period.getExtensions().entrySet()) {
				String group = entry.getKey();
				Map<String, double[]> groups = m_extensions.get(group);

				if (groups == null) {
					groups = new LinkedHashMap<String, double[]>();

					m_extensions.put(group, groups);
				}
				for (Entry<String, Detail> detail : entry.getValue().getDetails().entrySet()) {
					String key = detail.getKey();
					double[] doubles = groups.get(key);

					if (doubles == null) {
						doubles = new double[60];
						groups.put(key, doubles);
					}

					doubles[minute] = detail.getValue().getValue();
				}
			}
		}
		m_extensions = dealWithExtensions();
		return this;
	}

	private double[] getAddedCount(double[] source) {
		double[] result = new double[60];

		for (int i = 1; i <= 59; i++) {
			if (source[i - 1] > 0) {
				double d = source[i] - source[i - 1];
				if (d < 0) {
					d = source[i];
				}
				result[i] = d;
			}
		}
		return result;
	}

	public GraphBuilder getBuilder() {
		return m_builder;
	}

	public Map<String, ExtensionGroup> getExtensionGraph() {
		Map<String, ExtensionGroup> graphs = new LinkedHashMap<String, ExtensionGroup>();

		for (Entry<String, Map<String, double[]>> items : m_extensions.entrySet()) {
			buildExtensionGraph(graphs, items);
		}

		return graphs;
	}

	public class ExtensionGroup {

		private Map<String, String> m_svgs = new LinkedHashMap<String, String>();

		public int getHeight() {
			if (m_svgs != null) {
				int size = m_svgs.size();

				if (size % 3 == 0) {
					return size / 3;
				} else {
					return size / 3 + 1;
				}
			} else {
				return 0;
			}
		}

		public Map<String, String> getSvgs() {
			return m_svgs;
		}
	}

}
