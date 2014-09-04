package com.dianping.cat.report.task.overload;

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
	}

}
