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
package com.dianping.cat.system.page.router.config;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.GroupServer;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.entity.ServerGroup;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.system.page.router.service.RouterConfigService;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

@Named
public class RouterConfigHandler implements LogEnabled {

	protected Logger m_logger;

	@Inject
	private StateReportService m_stateReportService;

	@Inject
	private RouterConfigManager m_configManager;

	@Inject
	private RouterConfigService m_reportService;

	@Inject
	private DailyReportDao m_dailyReportDao;

	private void addServerList(List<Server> servers, Server server) {
		for (Server s : servers) {
			if (s.getId().equals(server.getId())) {
				return;
			}
		}
		servers.add(server);
	}

	public RouterConfig buildRouterConfig(String domain, Date period) {
		Date end = new Date(period.getTime() + TimeHelper.ONE_DAY);
		StateReport report = m_stateReportService.queryReport(Constants.CAT, period, end);
		RouterConfig routerConfig = new RouterConfig(Constants.CAT);
		StateReportVisitor visitor = new StateReportVisitor(m_configManager);

		visitor.visitStateReport(report);

		Map<String, Map<String, Long>> statistics = visitor.getStatistics();
		Map<String, Map<Server, Long>> servers = findAvaliableGpToSvrs();

		for (Entry<String, Map<Server, Long>> entry : servers.entrySet()) {
			String group = entry.getKey();
			Map<String, Long> numbers = statistics.get(group);

			if (numbers != null) {
				processMainServer(entry.getValue(), routerConfig, numbers, group);
			}
		}

		for (Entry<String, Map<Server, Long>> entry : servers.entrySet()) {
			String group = entry.getKey();
			Map<String, Long> numbers = statistics.get(group);

			if (numbers != null) {
				processBackServer(entry.getValue(), routerConfig, numbers, group);
			}

		}

		for (Entry<String, Map<Server, Long>> entry : servers.entrySet()) {
			for (Entry<Server, Long> e : entry.getValue().entrySet()) {
				Cat.logEvent("RouterConfig", entry.getKey() + ":" + e.getKey().getId() + ":" + e.getValue(), Event.SUCCESS,	null);
			}
		}

		routerConfig.setStartTime(period);
		routerConfig.setEndTime(end);

		return routerConfig;
	}

	private boolean checkDomainConfig(String group, Domain defaultDomainConfig) {
		return defaultDomainConfig == null || defaultDomainConfig.findGroup(group) == null	|| defaultDomainConfig
								.findGroup(group).getServers().isEmpty();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private Map<String, Map<Server, Long>> findAvaliableGpToSvrs() {
		Map<String, Map<Server, Long>> results = new HashMap<String, Map<Server, Long>>();
		Map<String, Server> servers = m_configManager.queryEnableServers();
		RouterConfig routerConfig = m_configManager.getRouterConfig();
		Map<String, ServerGroup> groups = routerConfig.getServerGroups();

		for (Entry<String, NetworkPolicy> entry : routerConfig.getNetworkPolicies().entrySet()) {
			NetworkPolicy networkPolicy = entry.getValue();
			ServerGroup serverGroup = groups.get(networkPolicy.getServerGroup());

			if (!networkPolicy.isBlock() && serverGroup != null) {
				Map<Server, Long> networkResults = new HashMap<Server, Long>();

				for (GroupServer s : serverGroup.getGroupServers().values()) {
					Server server = servers.get(s.getId());

					if (server != null) {
						networkResults.put(server, 0L);
					}
				}
				results.put(entry.getKey(), networkResults);
			}
		}
		return results;
	}

	private Server findMinServer(Map<Server, Long> servers) {
		long min = Long.MAX_VALUE;
		Server result = null;

		for (Entry<Server, Long> entry : servers.entrySet()) {
			Server server = entry.getKey();
			Long value = (long) (entry.getValue() / server.getWeight());

			if (value < min) {
				result = entry.getKey();
				min = value;
			}
		}
		return result;
	}

	private Map<Server, Long> findOrCreateBacks(Map<Server, Long> servers, Map<Server, Map<Server, Long>> backServers,
							Server server) {
		Map<Server, Long> serverProcess = backServers.get(server);

		if (serverProcess == null) {
			serverProcess = new LinkedHashMap<Server, Long>();

			for (Entry<Server, Long> entry : servers.entrySet()) {
				if (!entry.getKey().equals(server)) {
					serverProcess.put(entry.getKey(), entry.getValue());
				}
			}
			backServers.put(server, serverProcess);
		}
		return serverProcess;
	}

	private void processBackServer(Map<Server, Long> servers, RouterConfig routerConfig, Map<String, Long> statistics,
							String group) {
		Map<Server, Map<Server, Long>> backServers = new LinkedHashMap<Server, Map<Server, Long>>();
		Server backUpServer = m_configManager.queryBackUpServer();
		Collection<Domain> values = routerConfig.getDomains().values();

		for (Domain domain : values) {
			try {
				Group serverGroup = domain.findGroup(group);

				if (serverGroup != null && !serverGroup.getServers().isEmpty()) {
					List<Server> domainServers = serverGroup.getServers();
					Domain defaultDomainConfig = m_configManager.getRouterConfig().findDomain(domain.getId());

					if (checkDomainConfig(group, defaultDomainConfig)) {
						Server mainServer = serverGroup.getServers().get(0);
						Map<Server, Long> backs = findOrCreateBacks(servers, backServers, mainServer);
						Server secondServer = findMinServer(backs);

						if (secondServer != null) {
							Long oldValue = backs.get(secondServer);

							backs.put(secondServer, oldValue + statistics.get(domain.getId()));
							addServerList(domainServers, secondServer);
						}
						addServerList(domainServers, backUpServer);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private void processMainServer(Map<Server, Long> servers, RouterConfig routerConfig, Map<String, Long> statistics,
							String group) {
		for (Entry<String, Long> entry : statistics.entrySet()) {
			try {
				String domainName = entry.getKey();
				Domain defaultDomainConfig = m_configManager.getRouterConfig().findDomain(domainName);
				Long value = entry.getValue();

				if (checkDomainConfig(group, defaultDomainConfig)) {
					Domain domainConfig = routerConfig.findOrCreateDomain(domainName);
					Server server = findMinServer(servers);

					if (server != null) {
						Group serverGroup = domainConfig.findOrCreateGroup(group);
						Long oldValue = servers.get(server);

						serverGroup.addServer(server);
						servers.put(server, oldValue + value);
					}
				} else {
					Domain domainConfig = routerConfig.findOrCreateDomain(domainName);
					Group serverGroup = defaultDomainConfig.findGroup(group);

					domainConfig.addGroup(serverGroup);

					Server server = serverGroup.getServers().get(0);
					Long oldValue = servers.get(server);

					if (oldValue != null) {
						servers.put(server, oldValue + value);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	public boolean updateRouterConfig(Date period) {
		try {
			String name = RouterConfigBuilder.ID;
			String domain = Constants.CAT;
			RouterConfig routerConfig = buildRouterConfig(domain, period);
			DailyReport dailyReport = new DailyReport();

			dailyReport.setCreationDate(new Date());
			dailyReport.setDomain(domain);
			dailyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			dailyReport.setName(name);
			dailyReport.setPeriod(period);
			dailyReport.setType(1);

			m_dailyReportDao.deleteByDomainNamePeriod(dailyReport);
			byte[] binaryContent = DefaultNativeBuilder.build(routerConfig);

			m_reportService.insertDailyReport(dailyReport, binaryContent);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

}
