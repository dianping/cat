package com.dianping.cat.report.page.overload.task;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.MonthlyReportContentDao;
import com.dianping.cat.core.dal.MonthlyReportContentEntity;
import com.dianping.cat.home.dal.report.Overload;
import com.dianping.cat.home.dal.report.OverloadDao;

public class MonthlyCapacityUpdater implements CapacityUpdater {

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Inject
	private MonthlyReportContentDao m_monthlyReportContentDao;

	@Inject
	private OverloadDao m_overloadDao;

	@Inject
	private CapacityUpdateStatusManager m_manager;

	public static final String ID = "monthly_capacity_updater";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void updateDBCapacity() throws DalException {
		int maxId = m_manager.getMonthlyStatus();

		while (true) {
			List<MonthlyReportContent> reports = m_monthlyReportContentDao.findOverloadReport(maxId,
			      MonthlyReportContentEntity.READSET_LENGTH);

			for (MonthlyReportContent content : reports) {
				try {
					int reportId = content.getReportId();
					double contentLength = content.getContentLength();

					if (contentLength >= CapacityUpdater.CAPACITY) {
						Overload overload = m_overloadDao.createLocal();

						overload.setReportId(reportId);
						overload.setReportSize(contentLength);
						overload.setReportType(CapacityUpdater.MONTHLY_TYPE);

						try {
							MonthlyReport report = m_monthlyReportDao.findByPK(reportId, MonthlyReportEntity.READSET_FULL);
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
		m_manager.updateMonthlyStatus(maxId);
	}

}
