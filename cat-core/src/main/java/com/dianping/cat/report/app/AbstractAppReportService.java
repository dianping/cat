package com.dianping.cat.report.app;

import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDailyReport;
import com.dianping.cat.app.AppDailyReportContent;
import com.dianping.cat.app.AppDailyReportContentDao;
import com.dianping.cat.app.AppDailyReportDao;

public abstract class AbstractAppReportService<T> implements LogEnabled, AppReportService<T> {

	@Inject
	protected AppDailyReportDao m_dailyReportDao;

	@Inject
	protected AppDailyReportContentDao m_dailyReportContentDao;

	protected Logger m_logger;

	public static final int s_daily = 2;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public boolean insertDailyReport(AppDailyReport report, byte[] content) {
		try {
			m_dailyReportDao.insert(report);

			int id = report.getId();
			AppDailyReportContent proto = m_dailyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			proto.setCreationDate(new Date());
			m_dailyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	public abstract T makeReport(String domain, Date start, Date end);

	@Override
	public abstract T queryDailyReport(int namespace, Date start, Date end);

}
