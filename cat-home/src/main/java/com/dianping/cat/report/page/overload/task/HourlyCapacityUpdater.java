package com.dianping.cat.report.page.overload.task;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
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

public class HourlyCapacityUpdater implements CapacityUpdater {

	@Inject
	private HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	public static final String ID = "hourly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getHourlyStatus();

		while (true) {
			List<HourlyReportContent> reports = m_hourlyReportContentDao.findOverloadReport(maxId,
			      HourlyReportContentEntity.READSET_LENGTH);

			for (HourlyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.HOURLY_TYPE);

						HourlyReport hourlyReport;
						try {
							hourlyReport = m_hourlyReportDao.findByPK(reportId, HourlyReportEntity.READSET_FULL);
							overload.setPeriod(hourlyReport.getPeriod());
							m_overloadDao.insert(overload);

						} catch (DalNotFoundException e) {
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}

			int size = reports.size();
			if (size == 0) {
				break;
			} else {
				maxId = reports.get(size - 1).getReportId();
			}
		}
		m_manager.updateHourlyStatus(maxId);
	}

}
