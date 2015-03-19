package com.dianping.cat.system.page.router.task;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.RouterConfigService;

public class RouterConfigBuilder implements TaskBuilder, LogEnabled {

	public static final String ID = Constants.REPORT_ROUTER;

	@Inject
	private RouterConfigService m_reportService;
	
	@Inject
	private StateReportService m_stateReportService;

	@Inject
	private RouterConfigManager m_configManager;

	protected Logger m_logger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Date end = new Date(period.getTime() + TimeHelper.ONE_DAY);
		StateReport report = m_stateReportService.queryReport(Constants.CAT, period, end);
		RouterConfig routerConfig = new RouterConfig(Constants.CAT);
		StateReportVisitor visitor = new StateReportVisitor();

		visitor.visitStateReport(report);

		Map<String, Long> numbers = visitor.getNumbers();
		Comparator<Entry<String, Long>> compator = new Comparator<Map.Entry<String, Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				long value = o2.getValue() - o1.getValue();

				if (value > 0) {
					return 1;
				} else if (value < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		numbers = SortHelper.sortMap(numbers, compator);
		Map<Server, Long> servers = findAvaliableServers();

		processMainServer(servers, routerConfig, numbers);

		for (Entry<Server, Long> entry : servers.entrySet()) {
			Cat.logEvent("RouterConfig", entry.getKey().getId() + ":" + entry.getValue(), Event.SUCCESS, null);
		}

		processBackServer(servers, routerConfig, numbers);

		routerConfig.setStartTime(end);
		routerConfig.setEndTime(new Date(end.getTime() + TimeHelper.ONE_DAY));
		DailyReport dailyReport = new DailyReport();

		dailyReport.setCreationDate(new Date());
		dailyReport.setDomain(domain);
		dailyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		dailyReport.setName(name);
		dailyReport.setPeriod(end);
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

	private Server findMinServer(Map<Server, Long> maps) {
		long min = Long.MAX_VALUE;
		Server result = null;

		for (Entry<Server, Long> entry : maps.entrySet()) {
			Server server = entry.getKey();
			Long value = (long) (entry.getValue() / server.getWeight());

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
				Server nextServer = findMinServer(serverProcess);

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
				Server server = findMinServer(servers);
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
			long total = (int) processDomain.getTotal();
			Long count = m_numbers.get(domain);

			if (count == null) {
				m_numbers.put(domain, total);
			} else {
				m_numbers.put(domain, total + count);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
