package com.dianping.cat.report.task.overload;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
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
	MonthlyReportDao m_monthlyReportDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 4;

	public static final String ID = "monthly_capacity_updater";

	private OverloadReport generateOverloadReport(MonthlyReport report, OverloadTable table) {
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
		int loopStartId = maxId;
		boolean hasMore = true;

		while (hasMore) {
			List<MonthlyReportContent> monthlyReports = m_monthlyReportContentDao.findOverloadReport(loopStartId,
			      capacity, MonthlyReportContentEntity.READSET_LENGTH);

			for (MonthlyReportContent content : monthlyReports) {
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

			int size = monthlyReports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = monthlyReports.get(size - 1).getReportId();
			}
		}

		return maxId;
	}

	@Override
	public void updateOverloadReport(int updateStartId, List<OverloadReport> overloadReports) throws DalException {
		boolean hasMore = true;

		while (hasMore) {
			List<OverloadTable> overloadTables = m_overloadTableDao.findIdAndSizeByTypeAndBeginId(TYPE, updateStartId,
			      OverloadTableEntity.READSET_BIGGER_ID_SIZE);

			for (OverloadTable table : overloadTables) {
				try {
					int reportId = table.getReportId();
					MonthlyReport report = m_monthlyReportDao.findByPK(reportId, MonthlyReportEntity.READSET_FULL);

					overloadReports.add(generateOverloadReport(report, table));
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			}
			if (overloadTables.size() < 1000) {
				hasMore = false;
			}
		}
	}
}
