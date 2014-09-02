package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;

public class WeeklyCapacityUpdater implements CapacityUpdater {

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	private OverloadDao m_overloadDao;

	public static final String ID = "weekly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadDao.findMaxIdByType(CapacityUpdater.WEEKLY_TYPE, OverloadEntity.READSET_MAXID).getMaxId();
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<WeeklyReportContent> weeklyReports = m_weeklyReportContentDao.findOverloadReport(loopStartId, capacity,
			      WeeklyReportContentEntity.READSET_LENGTH);

			for (WeeklyReportContent content : weeklyReports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();
					Overload overload = m_overloadDao.createLocal();

					overload.setReportId(reportId);
					overload.setReportSize(contentLength);
					overload.setReportType(CapacityUpdater.WEEKLY_TYPE);

					WeeklyReport weeklyReport = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);
					overload.setPeriod(weeklyReport.getPeriod());

					m_overloadDao.insert(overload);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = weeklyReports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = weeklyReports.get(size - 1).getReportId();
			}
		}
	}

}
