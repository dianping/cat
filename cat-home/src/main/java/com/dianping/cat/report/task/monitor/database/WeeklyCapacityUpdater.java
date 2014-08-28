package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

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
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 3;

	public static final String ID = "weekly_capacity_updater";

	@Override
	public void updateCapacity(double capacity) {
		try {
			int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
			List<WeeklyReportContent> weeklyReports = m_weeklyReportContentDao.findOverloadReport(maxId, capacity,
			      WeeklyReportContentEntity.READSET_LENGTH);

			for (WeeklyReportContent content : weeklyReports) {
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
