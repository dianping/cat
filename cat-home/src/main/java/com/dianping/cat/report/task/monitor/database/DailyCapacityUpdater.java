package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.home.OverloadReport.entity.OverloadReport;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.OverloadTable;
import com.dianping.cat.home.dal.report.OverloadTableDao;
import com.dianping.cat.home.dal.report.OverloadTableEntity;

public class DailyCapacityUpdater implements CapacityUpdater {

	@Inject
	DailyReportContentDao m_dailyReportContentDao;

	@Inject
	DailyReportDao m_dailyReportDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 2;

	public static final String ID = "daily_capacity_updater";

	private OverloadReport generateOverloadReport(DailyReport report) {
		OverloadReport overloadReport = new OverloadReport();

		overloadReport.setDomain(report.getDomain());
		overloadReport.setIp(report.getIp());
		overloadReport.setName(report.getName());
		overloadReport.setPeriod(report.getPeriod());
		overloadReport.setReportType(TYPE);
		overloadReport.setType(report.getType());

		return overloadReport;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
		List<DailyReportContent> dailyReports = m_dailyReportContentDao.findOverloadReport(maxId, capacity,
		      DailyReportContentEntity.READSET_LENGTH);

		for (DailyReportContent content : dailyReports) {
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
	public void updateOverloadReport(int updateBStartId, List<OverloadReport> overloadReports) throws DalException {
		List<OverloadTable> overloadTables = m_overloadTableDao.findIdByTypeAndBeginId(TYPE, updateBStartId,
		      OverloadTableEntity.READSET_BIGGER_ID);

		for (OverloadTable table : overloadTables) {
			try {
				int reportId = table.getReportId();
				DailyReport report = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);

				overloadReports.add(generateOverloadReport(report));
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
	}

}
