package com.dianping.cat.system.page.router.task;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeBuilder;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.system.page.router.config.RouterConfigAdjustor;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.service.RouterConfigService;

@Named(type = TaskBuilder.class, value = RouterConfigBuilder.ID)
public class RouterConfigBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_ROUTER;

	@Inject
	private RouterConfigHandler m_routerConfigHandler;

	@Inject
	private RouterConfigAdjustor m_routerAdjustor;

	@Inject
	private RouterConfigService m_reportService;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		RouterConfig routerConfig = m_routerConfigHandler.buildRouterConfig(domain, period);
		DailyReport dailyReport = new DailyReport();

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
		if (m_serverConfigManager.isRouterAdjustEnabled()) {
			m_routerAdjustor.Adjust(period);
		}
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder doesn't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder doesn't support weekly task");
	}

}
