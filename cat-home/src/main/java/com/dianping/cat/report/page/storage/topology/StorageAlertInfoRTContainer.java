package com.dianping.cat.report.page.storage.topology;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;

public class StorageAlertInfoRTContainer {

	private Map<Long, StorageAlertInfo> m_alertInfos = new LinkedHashMap<Long, StorageAlertInfo>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<Long, StorageAlertInfo> eldest) {
			return size() > 60;
		}

	};

	public StorageAlertInfo findOrCreate(long time) {
		StorageAlertInfo report = m_alertInfos.get(time);

		if (report == null) {
			report = makeAlertInfo("SQL", new Date(time));
			m_alertInfos.put(time, report);
		}
		return report;
	}

	public StorageAlertInfo find(long time, int minute) {
		return m_alertInfos.get(time + minute * TimeHelper.ONE_MINUTE);
	}

	public StorageAlertInfo makeAlertInfo(String id, Date start) {
		StorageAlertInfo report = new StorageAlertInfo(id);

		report.setStartTime(start);
		report.setEndTime(new Date(start.getTime() + TimeHelper.ONE_MINUTE - 1));
		return report;
	}
}