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

	private OverloadReport generateOverloadReport(DailyReport report, Overload overload) {
		OverloadReport overloadReport = new OverloadReport();

		overloadReport.setDomain(report.getDomain());
		overloadReport.setIp(report.getIp());
		overloadReport.setName(report.getName());
		overloadReport.setPeriod(report.getPeriod());
		overloadReport.setReportType(CapacityUpdater.DAILY_TYPE);
		overloadReport.setType(report.getType());
		overloadReport.setReportLength(overload.getReportSize());

		return overloadReport;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int updateDBCapacity(double capacity) throws DalException {
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

		return maxId;
	}

	@Override
	public void updateOverloadReport(int updateStartId, List<OverloadReport> overloadReports) throws DalException {
		boolean hasMore = true;

		while (hasMore) {
			List<Overload> overloads = m_overloadDao.findIdAndSizeByTypeAndBeginId(CapacityUpdater.DAILY_TYPE,
			      updateStartId, OverloadEntity.READSET_BIGGER_ID_SIZE);

			for (Overload overload : overloads) {
				try {
					int reportId = overload.getReportId();
					DailyReport report = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);

					overloadReports.add(generateOverloadReport(report, overload));
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			if (overloads.size() < 1000) {
				hasMore = false;
			}
		}
	}

}
