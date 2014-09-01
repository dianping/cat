package com.dianping.cat.report.task.database;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.OverloadReport.entity.OverloadReport;
import com.dianping.cat.home.dal.report.OverloadTable;
import com.dianping.cat.home.dal.report.OverloadTableDao;
import com.dianping.cat.home.dal.report.OverloadTableEntity;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentEntity;

public class WeeklyCapacityUpdater implements CapacityUpdater {

	@Inject
	WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	WeeklyReportDao m_weeklyReportDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 3;

	public static final String ID = "weekly_capacity_updater";

	private OverloadReport generateOverloadReport(WeeklyReport report, OverloadTable table) {
		OverloadReport overloadReport = new OverloadReport();

		overloadReport.setDomain(report.getDomain());
		overloadReport.setIp(report.getIp());
		overloadReport.setName(report.getName());
		overloadReport.setPeriod(report.getPeriod());
		overloadReport.setReportType(TYPE);
		overloadReport.setType(report.getType());
		overloadReport.setReportLength(table.getReportSize());

		return overloadReport;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
		List<WeeklyReportContent> weeklyReports = m_weeklyReportContentDao.findOverloadReport(maxId, capacity,
		      WeeklyReportContentEntity.READSET_LENGTH);

		for (WeeklyReportContent content : weeklyReports) {
			try {
				int reportId = content.getReportId();
				double contentLength = content.getContentLength();
				OverloadTable overloadTable = m_overloadTableDao.createLocal();

				overloadTable.setReportId(reportId);
				overloadTable.setReportSize(contentLength);
				overloadTable.setReportType(TYPE);

				m_overloadTableDao.insert(overloadTable);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}

		return maxId;
	}

	@Override
	public void updateOverloadReport(int updateStartId, List<OverloadReport> overloadReports) throws DalException {
		List<OverloadTable> overloadTables = m_overloadTableDao.findIdAndSizeByTypeAndBeginId(TYPE, updateStartId,
		      OverloadTableEntity.READSET_BIGGER_ID_SIZE);

		for (OverloadTable table : overloadTables) {
			try {
				int reportId = table.getReportId();
				WeeklyReport report = m_weeklyReportDao.findByPK(reportId, WeeklyReportEntity.READSET_FULL);

				overloadReports.add(generateOverloadReport(report, table));
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
	}

}
