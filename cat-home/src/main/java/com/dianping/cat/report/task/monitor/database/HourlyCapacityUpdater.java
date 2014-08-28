package com.dianping.cat.report.task.monitor.database;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.home.dal.report.OverloadTable;
import com.dianping.cat.home.dal.report.OverloadTableDao;
import com.dianping.cat.home.dal.report.OverloadTableEntity;

public class HourlyCapacityUpdater implements CapacityUpdater {

	@Inject
	HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 1;

	public static final String ID = "hourly_capacity_updater";

	@Override
	public void updateCapacity(double capacity) {
		try {
			int maxId = m_overloadTableDao.findMaxIdByType(TYPE, OverloadTableEntity.READSET_MAXID).getMaxId();
			List<HourlyReportContent> reports = m_hourlyReportContentDao.findOverloadReport(maxId, capacity,
			      HourlyReportContentEntity.READSET_LENGTH);

			for (HourlyReportContent content : reports) {
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
