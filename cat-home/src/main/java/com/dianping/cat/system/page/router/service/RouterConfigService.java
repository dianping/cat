package com.dianping.cat.system.page.router.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultNativeParser;
import com.dianping.cat.report.service.AbstractReportService;

public class RouterConfigService extends AbstractReportService<RouterConfig> {

	private Map<Long, RouterConfig> m_configs = new HashMap<Long, RouterConfig>();

	@Override
	public RouterConfig makeReport(String domain, Date start, Date end) {
		return null;
	}

	@Override
	public RouterConfig queryDailyReport(String domain, Date start, Date end) {
		long time = start.getTime();
		RouterConfig config = m_configs.get(time);

		if (config == null) {
			String name = Constants.REPORT_ROUTER;

			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, start,
				      DailyReportEntity.READSET_FULL);
				
				config = queryFromDailyBinary(report.getId());

				if (config != null) {
					m_configs.put(time, config);
				}
				return config;
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		} else {
			return config;
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
