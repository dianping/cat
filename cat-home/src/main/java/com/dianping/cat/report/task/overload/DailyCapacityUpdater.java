package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;

public class DailyCapacityUpdater implements CapacityUpdater {

	@Inject
	private DailyReportContentDao m_dailyReportContentDao;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	public static final String ID = "daily_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadDao.findMaxIdByType(CapacityUpdater.DAILY_TYPE, OverloadEntity.READSET_MAXID).getMaxId();
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<DailyReportContent> dailyReports = m_dailyReportContentDao.findOverloadReport(loopStartId, capacity,
			      DailyReportContentEntity.READSET_LENGTH);

			for (DailyReportContent content : dailyReports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();
					Overload overload = m_overloadDao.createLocal();

					overload.setReportId(reportId);
					overload.setReportSize(contentLength);
					overload.setReportType(CapacityUpdater.DAILY_TYPE);

					DailyReport dailyReport = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);
					overload.setPeriod(dailyReport.getPeriod());

					m_overloadDao.insert(overload);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = dailyReports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = dailyReports.get(size - 1).getReportId();
			}
		}
	}

}
