package com.dianping.cat.report.service.impl;

import java.util.Date;

import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.report.service.AbstractReportService;

public class StorageReportService extends AbstractReportService<StorageReport> {

	@Override
	public StorageReport makeReport(String id, Date start, Date end) {
		StorageReport report = new StorageReport(id);
		int index = id.lastIndexOf("-");
		String name = id.substring(0, index);
		String type = id.substring(index + 1);

		report.setName(name).setType(type);
		report.setStartTime(start).setEndTime(end);
		return report;
	}

	@Override
	public StorageReport queryDailyReport(String id, Date start, Date end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageReport queryHourlyReport(String id, Date start, Date end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageReport queryMonthlyReport(String id, Date start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StorageReport queryWeeklyReport(String id, Date start) {
		// TODO Auto-generated method stub
		return null;
	}

}
