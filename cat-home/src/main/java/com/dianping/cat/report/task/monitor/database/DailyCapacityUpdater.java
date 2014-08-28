package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

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
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 2;

	public static final String ID = "daily_capacity_updater";

	@Override
	public void updateCapacity(double capacity) {
		try {
			int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
			List<DailyReportContent> dailyReports = m_dailyReportContentDao.findOverloadReport(maxId, capacity,
			      DailyReportContentEntity.READSET_LENGTH);

			for (DailyReportContent content : dailyReports) {
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
