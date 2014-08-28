package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.OverloadTable;
import com.dianping.cat.home.dal.report.OverloadTableDao;
import com.dianping.cat.home.dal.report.OverloadTableEntity;

public class MonthlyCapacityUpdater implements CapacityUpdater {

	@Inject
	MonthlyReportContentDao m_monthlyReportContentDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 4;

	public static final String ID = "monthly_capacity_updater";

	@Override
	public void updateCapacity(double capacity) {
		try {
			int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
			List<MonthlyReportContent> monthlyReports = m_monthlyReportContentDao.findOverloadReport(maxId, capacity,
			      MonthlyReportContentEntity.READSET_LENGTH);

			for (MonthlyReportContent content : monthlyReports) {
				int reportId = content.getReportId();
				double contentLength = content.getContentLength();
				OverloadTable overloadTable = m_overloadTableDao.createLocal();

				overloadTable.setReportId(reportId);
				overloadTable.setReportSize(contentLength);
				overloadTable.setReportType(TYPE);

				m_overloadTableDao.insert(overloadTable);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String getId() {
		return ID;
	}

}
