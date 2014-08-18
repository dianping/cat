package com.dianping.cat.report.task.router;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.system.config.RouterConfigManager;

public class RouterConfigBuilder implements ReportTaskBuilder {

	public static final String ID = Constants.REPORT_ROUTER;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private RouterConfigManager m_configManager;

	private boolean needRebuild(StateReport report, RouterConfig config) {
		if (config != null) {
			Map<String, Long> serverProcesses = new LinkedHashMap<String, Long>();
			StateReportVisitor visitor = new StateReportVisitor();
			visitor.visitStateReport(report);

			Map<String, Long> numbers = visitor.getNumbers();

			for (Entry<String, Long> entry : numbers.entrySet()) {
				String domain = entry.getKey();
				Long count = entry.getValue();
				Domain serverConfig = config.findDomain(domain);

				if (serverConfig != null) {
					Server server = serverConfig.getServers().get(0);
					String serverId = server.getId();
					Long value = serverProcesses.get(serverId);

					if (value == null) {
						serverProcesses.put(serverId, count);
					} else {
						serverProcesses.put(serverId, count + value);
					}
				}
			}

			long min = Integer.MAX_VALUE;
			long max = Integer.MIN_VALUE;

			for (Entry<String, Long> entry : serverProcesses.entrySet()) {
				long value = entry.getValue();

				if (value > max) {
					max = value;
				}
				if (value < min) {
					min = value;
				}
			}

			if (max * 1.0 / min > 1.4) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Date yesterday = new Date(period.getTime() - TimeUtil.ONE_DAY);
		RouterConfig yesterdayConfig = m_reportService.queryRouterConfigReport(Constants.CAT, yesterday, period);
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY);
		StateReport report = m_reportService.queryStateReport(Constants.CAT, start, end);

		boolean need = needRebuild(report, yesterdayConfig);
		RouterConfig routerConfig;

		if (need) {
			routerConfig = new RouterConfig(Constants.CAT);
			StateReportVisitor visitor = new StateReportVisitor();

			visitor.visitStateReport(report);

			Map<String, Long> numbers = visitor.getNumbers();
			Comparator<Entry<String, Long>> compator = new Comparator<Map.Entry<String, Long>>() {

				@Override
				public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
					return (int) (o2.getValue() - o1.getValue());
				}
			};
			numbers = MapUtils.sortMap(numbers, compator);
			Map<Server, Long> servers = findAvaliableServers();

			processMainServer(servers, routerConfig, numbers);
			processBackServer(servers, routerConfig, numbers);
		} else {
			routerConfig = yesterdayConfig;
		}
		routerConfig.setStartTime(start);
		routerConfig.setEndTime(end);

		DailyReport dailyReport = new DailyReport();

		dailyReport.setContent("");
		dailyReport.setCreationDate(new Date());
		dailyReport.setDomain(domain);
		dailyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		dailyReport.setName(name);
		dailyReport.setPeriod(period);
		dailyReport.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(routerConfig);

		m_reportService.insertDailyReport(dailyReport, binaryContent);
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support weekly task");
	}

	private Map<Server, Long> findAvaliableServers() {
		List<Server> servers = m_configManager.queryEnableServers();
		Map<Server, Long> result = new HashMap<Server, Long>();

		for (Server server : servers) {
			result.put(server, 0L);
		}
		return result;
	}

	private Server findMinProcessServer(Map<Server, Long> maps) {
		long min = Long.MAX_VALUE;
		Server result = null;

		for (Entry<Server, Long> entry : maps.entrySet()) {
			Long value = entry.getValue();

			if (value < min) {
				result = entry.getKey();
				min = value;
			}
		}
		return result;
	}

	private void addServerList(List<Server> servers, Server server) {
		for (Server s : servers) {
			if (s.getId().equals(server.getId())) {
				return;
			}
		}
		servers.add(server);
	}

	private void processBackServer(Map<Server, Long> servers, RouterConfig routerConfig, Map<String, Long> numbers) {
		Map<Server, Map<Server, Long>> backServers = new LinkedHashMap<Server, Map<Server, Long>>();
		Server backUpServer = m_configManager.queryBackUpServer();
		Collection<Domain> values = routerConfig.getDomains().values();

		for (Domain domain : values) {
			List<Server> domainServers = domain.getServers();
			String domainName = domain.getId();
			Domain defaultDomainConfig = m_configManager.getRouterConfig().getDomains().get(domainName);

			if (defaultDomainConfig == null) {
				Server server = domain.getServers().get(0);
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
				Server nextServer = findMinProcessServer(serverProcess);

				if (nextServer != null) {
					Long oldValue = serverProcess.get(nextServer);

					serverProcess.put(nextServer, oldValue + numbers.get(domain.getId()));

					addServerList(domainServers, nextServer);
				}
				addServerList(domainServers, backUpServer);
			}
		}
	}

	private void processMainServer(Map<Server, Long> servers, RouterConfig routerConfig, Map<String, Long> numbers) {
		for (Entry<String, Long> entry : numbers.entrySet()) {
			String domainName = entry.getKey();
			Domain defaultDomainConfig = m_configManager.getRouterConfig().getDomains().get(domainName);
			Long value = entry.getValue();

			if (defaultDomainConfig == null) {
				Server server = findMinProcessServer(servers);
				Long oldValue = servers.get(server);
				Domain domainConfig = new Domain(domainName);

				servers.put(server, oldValue + value);
				domainConfig.addServer(server);
				routerConfig.addDomain(domainConfig);
			} else {
				routerConfig.addDomain(defaultDomainConfig);

				Server server = defaultDomainConfig.getServers().get(0);
				Long oldValue = servers.get(server);

				if (oldValue != null) {
					servers.put(server, oldValue + value);
				}
			}
		}
	}

	public static class StateReportVisitor extends BaseVisitor {

		private Map<String, Long> m_numbers = new HashMap<String, Long>();

		public Map<String, Long> getNumbers() {
			return m_numbers;
		}

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			long total = processDomain.getTotal();
			Long count = m_numbers.get(domain);

			if (count == null) {
				m_numbers.put(domain, total);
			} else {
				m_numbers.put(domain, total + count);
			}
		}
	}

}
