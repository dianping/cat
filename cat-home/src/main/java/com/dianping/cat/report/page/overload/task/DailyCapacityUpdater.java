package com.dianping.cat.report.page.overload.task;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;

public class DailyCapacityUpdater implements CapacityUpdater {

	@Inject
	private DailyReportContentDao m_dailyReportContentDao;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	public static final String ID = "daily_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getDailyStatus();

		while (true) {
			List<DailyReportContent> reports = m_dailyReportContentDao.findOverloadReport(maxId,
			      DailyReportContentEntity.READSET_LENGTH);

			for (DailyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.DAILY_TYPE);

						try {
							DailyReport report = m_dailyReportDao.findByPK(reportId, DailyReportEntity.READSET_FULL);
							overload.setPeriod(report.getPeriod());
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
		m_manager.updateDailyStatus(maxId);
	}

}
