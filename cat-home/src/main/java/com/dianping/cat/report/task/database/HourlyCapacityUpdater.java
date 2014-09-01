package com.dianping.cat.report.task.database;

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
import com.dianping.cat.home.OverloadReport.entity.OverloadReport;
import com.dianping.cat.home.dal.report.OverloadTable;
import com.dianping.cat.home.dal.report.OverloadTableDao;
import com.dianping.cat.home.dal.report.OverloadTableEntity;

public class HourlyCapacityUpdater implements CapacityUpdater {

	@Inject
	HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	HourlyReportDao m_hourlyReportDao;

	@Inject
	OverloadTableDao m_overloadTableDao;

	private static final int TYPE = 1;

	public static final String ID = "hourly_capacity_updater";

	private OverloadReport generateOverloadReport(HourlyReport report, OverloadTable table) {
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
			List<HourlyReportContent> reports = m_hourlyReportContentDao.findOverloadReport(loopStartId, capacity,
			      HourlyReportContentEntity.READSET_LENGTH);

			for (HourlyReportContent content : reports) {
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

			int size = reports.size();
			if (size < 1000) {
				hasMore = false;
			} else {
				loopStartId = reports.get(size - 1).getReportId();
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
					HourlyReport report = m_hourlyReportDao.findByPK(reportId, HourlyReportEntity.READSET_FULL);

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
