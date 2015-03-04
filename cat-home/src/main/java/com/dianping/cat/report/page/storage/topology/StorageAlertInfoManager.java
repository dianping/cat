package com.dianping.cat.report.page.storage.topology;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.alert.report.storage.entity.StorageAlertReport;

public class StorageAlertInfoManager {

	private Map<Long, StorageAlertReport> m_reports = new LinkedHashMap<Long, StorageAlertReport>();

	public StorageAlertReport findOrCreate(long time) {
		StorageAlertReport report = m_reports.get(time);

		if (report == null) {
			report = makeReport("SQL", new Date(time), new Date(time + TimeHelper.ONE_HOUR - 1));
			m_reports.put(time, report);
		}
		return report;
	}

	public StorageAlertReport makeReport(String id, Date start, Date end) {
		StorageAlertReport report = new StorageAlertReport(id);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	// public StorageAlertReport getAlertReport(long time) {
	// StorageAlertReport report = m_reports.get(time);
	// if(report==null){
	// report = m_service.
	// }
	//
	// }
}
