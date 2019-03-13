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
package com.dianping.cat.report.page.dependency.task;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = DependencyReportBuilder.ID)
public class DependencyReportBuilder implements TaskBuilder {

	public static final String ID = DependencyAnalyzer.ID;

	@Inject
	private DependencyReportService m_reportService;

	@Inject
	private TopologyGraphBuilder m_graphBuilder;

	@Inject
	private TopologyGraphDao m_topologyGraphDao;

	@Override
	public boolean buildDailyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no daily report builder for dependency!");
	}

	@Override
	public boolean buildHourlyTask(String name, String reportDomain, Date reportPeriod) {
		Date end = new Date(reportPeriod.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(reportPeriod, end, DependencyAnalyzer.ID);
		boolean result = true;

		m_graphBuilder.getGraphs().clear();
		for (String domain : domains) {
			DependencyReport report = m_reportService.queryReport(domain, reportPeriod, end);

			m_graphBuilder.visitDependencyReport(report);
		}

		Map<Long, TopologyGraph> graphs = m_graphBuilder.getGraphs();
		for (Entry<Long, TopologyGraph> entry : graphs.entrySet()) {
			try {
				Date date = new Date(entry.getKey());
				TopologyGraph graph = entry.getValue();

				com.dianping.cat.home.dal.report.TopologyGraph proto = m_topologyGraphDao.createLocal();
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				proto.setType(3);
				proto.setPeriod(date);
				proto.setCreationDate(new Date());
				proto.setIp(ip);
				proto.setContent(DefaultNativeBuilder.build(graph));

				m_topologyGraphDao.insert(proto);
			} catch (Exception e) {
				result = false;
				Cat.logError(e);
			}
		}
		return result;
	}

	@Override
	public boolean buildMonthlyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no month report builder for dependency!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no week report builder for dependency!");
	}

}
