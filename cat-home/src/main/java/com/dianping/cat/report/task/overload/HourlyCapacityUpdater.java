package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;
import com.dianping.cat.home.dal.report.OverloadEntity;

public class HourlyCapacityUpdater implements CapacityUpdater {

	@Inject
	private HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	public static final String ID = "hourly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity(double capacity) throws DalException {
		int maxId = m_overloadDao.findMaxIdByType(CapacityUpdater.HOURLY_TYPE, OverloadEntity.READSET_MAXID).getMaxId();
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<HourlyReportContent> reports = m_hourlyReportContentDao.findOverloadReport(loopStartId, capacity,
			      HourlyReportContentEntity.READSET_LENGTH);

			for (HourlyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();
					Overload overload = m_overloadDao.createLocal();

					overload.setReportId(reportId);
					overload.setReportSize(contentLength);
					overload.setReportType(CapacityUpdater.HOURLY_TYPE);

					HourlyReport hourlyReport = m_hourlyReportDao.findByPK(reportId, HourlyReportEntity.READSET_FULL);
					overload.setPeriod(hourlyReport.getPeriod());

					m_overloadDao.insert(overload);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = reports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = reports.get(size - 1).getReportId();
			}
		}
	}

}
