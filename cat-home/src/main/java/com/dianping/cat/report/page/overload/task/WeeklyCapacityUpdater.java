package com.dianping.cat.report.page.overload.task;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.dal.WeeklyReportContentDao;
import com.dianping.cat.core.dal.WeeklyReportContentEntity;

public class WeeklyCapacityUpdater implements CapacityUpdater {

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	public static final String ID = "weekly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getWeeklyStatus();

		while (true) {
			List<WeeklyReportContent> reports = m_weeklyReportContentDao.findOverloadReport(maxId,
			      WeeklyReportContentEntity.READSET_LENGTH);

			for (WeeklyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.WEEKLY_TYPE);

						try {
							WeeklyReport report = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);
							overload.setPeriod(report.getPeriod());
							m_overloadDao.insert(overload);
						} catch (DalNotFoundException e) {
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = reports.size();
			if (size == 0) {
				break;
			} else {
				maxId = reports.get(size - 1).getReportId();
			}
		}
		m_manager.updateWeeklyStatus(maxId);
	}

}
