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
	private MonthlyReportContentDao m_monthlyReportContentDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	private static final int TYPE = 4;

	public static final String ID = "monthly_capacity_updater";

	private OverloadReport generateOverloadReport(MonthlyReport report, Overload overload) {
		OverloadReport overloadReport = new OverloadReport();

		overloadReport.setDomain(report.getDomain());
		overloadReport.setIp(report.getIp());
		overloadReport.setName(report.getName());
		overloadReport.setPeriod(report.getPeriod());
		overloadReport.setReportType(TYPE);
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
		int maxId = m_overloadDao.findMaxIdByType(TYPE, OverloadEntity.READSET_MAXID).getMaxId();
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
					overload.setReportType(TYPE);

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

		return maxId;
	}

	@Override
	public void updateOverloadReport(int updateStartId, List<OverloadReport> overloadReports) throws DalException {
		boolean hasMore = true;

		while (hasMore) {
			List<Overload> overloads = m_overloadDao.findIdAndSizeByTypeAndBeginId(TYPE, updateStartId,
			      OverloadEntity.READSET_BIGGER_ID_SIZE);

			for (Overload overload : overloads) {
				try {
					int reportId = overload.getReportId();
					MonthlyReport report = m_monthlyReportDao.findByPK(reportId, MonthlyReportEntity.READSET_FULL);

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
