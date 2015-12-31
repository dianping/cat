package com.dianping.cat.system.page.router.service;

import java.util.Date;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

public class RouterConfigService extends AbstractReportService<RouterConfig> {

	@Inject
	private RouterConfigManager m_routerConfigManager;

	@Override
	public RouterConfig makeReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public RouterConfig queryDailyReport(String domain, Date start, Date end) {
		long time = start.getTime();
		Map<Long, Pair<RouterConfig, Long>> routerConfigs = m_routerConfigManager.getRouterConfigs();
		Pair<RouterConfig, Long> pair = routerConfigs.get(time);

		if (pair == null) {
			String name = Constants.REPORT_ROUTER;

			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, start,
				      DailyReportEntity.READSET_FULL);
				RouterConfig config = queryFromDailyBinary(report.getId());

				routerConfigs.put(time, new Pair<RouterConfig, Long>(config, report.getCreationDate().getTime()));
				return config;
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		} else {
			return pair.getKey();
		}
	}

	private RouterConfig queryFromDailyBinary(int id) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return null;
		}
	}

	@Override
	public RouterConfig queryHourlyReport(String domain, Date start, Date end) {
		throw new RuntimeException("router report don't support hourly report");
	}

	@Override
	public RouterConfig queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support monthly report");
	}

	@Override
	public RouterConfig queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("router report don't support weekly report");
	}

}
