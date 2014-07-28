package com.dianping.cat.report.task.router;

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

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY);
		StateReport report = m_reportService.queryStateReport(Constants.CAT, start, end);
		StateReportVisitor visitor = new StateReportVisitor();
		RouterConfig routerConfig = new RouterConfig(Constants.CAT);

		routerConfig.setStartTime(period);
		routerConfig.setEndTime(new Date(period.getTime() + TimeUtil.ONE_DAY));
		visitor.visitStateReport(report);

		Map<String, Long> numbers = visitor.getNumbers();
		Comparator<Entry<String, Long>> compator = new Comparator<Map.Entry<String, Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return (int) (o2.getValue() - o1.getValue());
			}
		};
		numbers = MapUtils.sortMap(numbers, compator);
		Map<String, Long> servers = findAvaliableServers();

		processMainServer(servers, routerConfig, numbers);
		processBackServer(servers, routerConfig, numbers);

		DailyReport dailyReport = new DailyReport();

		dailyReport.setContent("");
		dailyReport.setCreationDate(new Date());
		dailyReport.setDomain(domain);
		dailyReport.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		dailyReport.setName(name);
		dailyReport.setPeriod(period);
		dailyReport.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(routerConfig);

		System.out.println(routerConfig);
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

	private Map<String, Long> findAvaliableServers() {
		List<String> servers = m_configManager.queryEnableServers();
		Map<String, Long> result = new HashMap<String, Long>();

		for (String server : servers) {
			result.put(server, 0L);
		}
		return result;
	}

	private String findMinProcessServer(Map<String, Long> maps) {
		long min = Long.MAX_VALUE;
		String result = null;

		for (Entry<String, Long> entry : maps.entrySet()) {
			Long value = entry.getValue();

			if (value < min) {
				result = entry.getKey();
				min = value;
			}
		}
		return result;
	}

	private void processBackServer(Map<String, Long> servers, RouterConfig routerConfig, Map<String, Long> numbers) {
		Map<String, Map<String, Long>> backServers = new LinkedHashMap<String, Map<String, Long>>();
		String backUpServer = m_configManager.queryBackUpServer();
		int port = m_configManager.queryPort();

		for (Domain domain : routerConfig.getDomains().values()) {
			String domainName = domain.getId();
			Domain defaultDomainConfig = m_configManager.getRouterConfig().getDomains().get(domainName);

			if (defaultDomainConfig == null) {

				String server = domain.getServers().get(0).getId();
				Map<String, Long> serverProcess = backServers.get(server);

				if (serverProcess == null) {
					serverProcess = new LinkedHashMap<String, Long>();

					for (Entry<String, Long> entry : servers.entrySet()) {
						if (!entry.getKey().equals(server)) {
							serverProcess.put(entry.getKey(), entry.getValue());
						}
					}
					backServers.put(server, serverProcess);
				}
				String nextServer = findMinProcessServer(serverProcess);
				Long oldValue = serverProcess.get(nextServer);

				serverProcess.put(nextServer, oldValue + numbers.get(domain.getId()));
				domain.addServer(new Server().setId(nextServer).setPort(port));
				domain.addServer(new Server().setId(backUpServer).setPort(port));
			}
		}
	}

	private void processMainServer(Map<String, Long> servers, RouterConfig routerConfig, Map<String, Long> numbers) {
		int port = m_configManager.queryPort();

		for (Entry<String, Long> entry : numbers.entrySet()) {
			String domainName = entry.getKey();
			Domain defaultDomainConfig = m_configManager.getRouterConfig().getDomains().get(domainName);
			Long value = entry.getValue();

			if (defaultDomainConfig == null) {
				String server = findMinProcessServer(servers);
				Long oldValue = servers.get(server);
				Domain domainConfig = new Domain(domainName);

				servers.put(server, oldValue + value);
				domainConfig.addServer(new Server().setId(server).setPort(port));
				routerConfig.addDomain(domainConfig);
			} else {
				routerConfig.addDomain(defaultDomainConfig);
				
				String server = defaultDomainConfig.getServers().get(0).getId();
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
