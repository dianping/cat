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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.Group;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.state.StateBuilder;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.system.page.router.service.RouterConfigService;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

@Named
public class RouterConfigAdjustor {

	@Inject
	private StateReportService m_stateReportService;

	@Inject
	private RouterConfigManager m_configManager;

	@Inject
	private RouterConfigService m_routerService;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private DailyReportDao m_dailyReportDao;

	public void Adjust(Date period) {
		Date end = new Date(period.getTime() + TimeHelper.ONE_HOUR);
		RouterConfig routerConfig = m_routerService.queryLastReport(Constants.CAT);
		StateReport report = m_stateReportService.queryHourlyReport(Constants.CAT, period, end);

		String remoteServers = m_serverConfigManager.getConsoleRemoteServers();
		List<String> servers = Splitters.by(",").noEmptyItem().split(remoteServers);

		AdjustStateReportVisitor visitor = new AdjustStateReportVisitor(m_configManager, servers);

		visitor.visitStateReport(report);

		Map<String, Map<String, Machine>> statistics = visitor.getStatistics();
		Map<String, Map<Server, Long>> gaps = buildGroupServersGaps(statistics);
		Map<String, Map<String, Server>> results = buildAdjustServers(gaps, routerConfig, statistics);

		updateRouterConfig(routerConfig, results);
		updateRouterConfigToDB(routerConfig);
	}

	private Map<String, Map<String, Server>> buildAdjustServers(Map<String, Map<Server, Long>> gaps,
							RouterConfig routerConfig, Map<String, Map<String, Machine>> statistics) {
		Map<String, Map<String, Server>> results = new HashMap<String, Map<String, Server>>();
		Map<String, Map<String, Long>> groupDomain2Gaps = buildGroupNeedAdjustDomains(gaps, statistics);

		for (Entry<String, Map<Server, Long>> entry : gaps.entrySet()) {
			String group = entry.getKey();
			Map<String, Long> ds = groupDomain2Gaps.get(group);
			Map<String, Server> map = fillWithAdjustDomains(entry.getValue(), ds);

			results.put(group, map);
		}

		return results;
	}

	private Map<String, Map<String, Long>> buildGroupNeedAdjustDomains(Map<String, Map<Server, Long>> gaps,
							Map<String, Map<String, Machine>> statistics) {
		Map<String, Map<String, Long>> results = new HashMap<String, Map<String, Long>>();
		Map<String, Map<String, Long>> datas = new HashMap<String, Map<String, Long>>();

		for (Entry<String, Map<Server, Long>> entry : gaps.entrySet()) {
			String group = entry.getKey();
			Map<String, Long> domains = buildNeedAdjustDomains(group, statistics.get(group), entry.getValue());

			datas.put(group, domains);
		}

		for (Entry<String, Map<String, Long>> data : datas.entrySet()) {
			String group = data.getKey();
			Map<String, Long> ds = SortHelper.sortMap(data.getValue(), new Comparator<Entry<String, Long>>() {

				@Override
				public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
					if (o2.getValue() > o1.getValue()) {
						return 1;
					} else if (o2.getValue() < o1.getValue()) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			results.put(group, ds);
		}
		return results;
	}

	private Map<String, Map<Server, Long>> buildGroupServersGaps(Map<String, Map<String, Machine>> statistics) {
		Map<String, Map<Server, Long>> results = new HashMap<String, Map<Server, Long>>();

		for (Entry<String, Map<String, Machine>> entry : statistics.entrySet()) {
			String group = entry.getKey();
			Map<Server, Long> gaps = processGroupMachines(entry.getValue());

			results.put(group, gaps);
		}
		return results;
	}

	private Map<String, Long> buildNeedAdjustDomains(String group, Map<String, Machine> statistics,
							Map<Server, Long> gaps) {
		Map<String, Long> datas = new HashMap<String, Long>();

		for (Entry<Server, Long> g : gaps.entrySet()) {
			long gap = g.getValue();

			if (gap > 0) {
				String ip = g.getKey().getId();
				Map<String, ProcessDomain> processDomains = statistics.get(ip).getProcessDomains();
				long sum = 0;

				for (Entry<String, ProcessDomain> e : processDomains.entrySet()) {
					long count = e.getValue().getTotal();
					String domain = e.getValue().getName();
					boolean noExist = m_configManager.notCustomizedDomains(group, domain);

					if (noExist && sum < gap && count <= (gap - sum)) {
						datas.put(domain, count);
						sum += count;
					}
				}
			}
		}
		return datas;
	}

	private long calculateTotal(Map<String, Machine> machines) {
		long total = 0;

		for (Entry<String, Machine> e : machines.entrySet()) {
			total += e.getValue().getTotal();
		}
		return total;
	}

	private Map<String, Server> fillWithAdjustDomains(Map<Server, Long> servers, Map<String, Long> ds) {
		Map<String, Server> results = new HashMap<String, Server>();

		for (Entry<Server, Long> g : servers.entrySet()) {
			long gap = g.getValue();

			if (gap < 0) {
				List<String> domains = findNearGapData(-gap, ds);

				for (String domain : domains) {
					results.put(domain, g.getKey());
				}
			}
		}
		return results;
	}

	private List<String> findNearGapData(long gap, Map<String, Long> datas) {
		ArrayList<String> rets = new ArrayList<String>();
		long sum = 0;

		for (Entry<String, Long> entry : datas.entrySet()) {
			long count = entry.getValue();

			if (sum < gap && count <= (gap - sum)) {
				rets.add(entry.getKey());

				sum += count;
			}
		}

		for (String domain : rets) {
			datas.remove(domain);
		}

		return rets;
	}

	private Map<Server, Long> processGroupMachines(Map<String, Machine> machines) {
		Map<Server, Long> results = new HashMap<Server, Long>();
		long total = calculateTotal(machines);
		long minGap = total / 100;
		long avg = total / machines.size();

		for (Machine machine : machines.values()) {
			long count = machine.getTotal();
			DefaultServer server = m_configManager.queryServerByIp(machine.getIp());
			double weight = server.getWeight();
			long gap = (long) (count / weight - avg);
			boolean loss = StateBuilder.checkTooMuchLoss(machine);

			if (Math.abs(gap) > minGap || loss) {
				if (loss) {
					gap = machine.getTotalLoss() * 2;
				} else {
					gap = (long) (gap < 0 ? gap * weight : gap / weight);
				}
				Server s = new Server();

				s.setId(server.getId()).setPort(server.getPort()).setWeight(weight);
				results.put(s, gap);
			}
		}
		return results;
	}

	private void updateRouterConfig(RouterConfig routerConfig, Map<String, Map<String, Server>> results) {
		for (Entry<String, Map<String, Server>> entry : results.entrySet()) {
			String group = entry.getKey();
			Map<String, Server> value = entry.getValue();

			for (Entry<String, Server> e : value.entrySet()) {
				Domain d = routerConfig.findDomain(e.getKey());

				if (d != null) {
					Group g = d.findGroup(group);

					if (g != null) {
						List<Server> servers = g.getServers();

						servers.set(0, e.getValue());
					}
				}
			}
		}
	}

	public boolean updateRouterConfigToDB(RouterConfig config) {
		try {
			String name = RouterConfigBuilder.ID;
			String domain = Constants.CAT;
			List<DailyReport> reports = m_dailyReportDao
									.queryLatestReportsByDomainName(domain, name, 1,	DailyReportEntity.READSET_FULL);
			DailyReport oldReport = reports.get(0);
			DailyReport dailyReport = new DailyReport();

			dailyReport.setCreationDate(new Date());
			dailyReport.setDomain(domain);
			dailyReport.setIp(oldReport.getIp());
			dailyReport.setName(name);
			dailyReport.setPeriod(oldReport.getPeriod());
			dailyReport.setType(oldReport.getType());
			m_dailyReportDao.deleteByPK(oldReport);

			byte[] binaryContent = DefaultNativeBuilder.build(config);

			m_routerService.insertDailyReport(dailyReport, binaryContent);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}
}
