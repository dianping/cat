package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;

public class MonthlyCapacityUpdater implements CapacityUpdater {

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Inject
	private MonthlyReportContentDao m_monthlyReportContentDao;

	@Inject
	private OverloadDao m_overloadDao;

	public static final String ID = "monthly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadDao.findMaxIdByType(CapacityUpdater.MONTHLY_TYPE, OverloadEntity.READSET_MAXID).getMaxId();
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<MonthlyReportContent> monthlyReports = m_monthlyReportContentDao.findOverloadReport(loopStartId,
			      capacity, MonthlyReportContentEntity.READSET_LENGTH);

			for (MonthlyReportContent content : monthlyReports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();
					Overload overload = m_overloadDao.createLocal();

					overload.setReportId(reportId);
					overload.setReportSize(contentLength);
					overload.setReportType(CapacityUpdater.MONTHLY_TYPE);

					MonthlyReport monthlyReport = m_monthlyReportDao.findByPK(reportId, MonthlyReportEntity.READSET_FULL);
					overload.setPeriod(monthlyReport.getPeriod());

					m_overloadDao.insert(overload);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = monthlyReports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = monthlyReports.get(size - 1).getReportId();
			}
		}
	}

}
