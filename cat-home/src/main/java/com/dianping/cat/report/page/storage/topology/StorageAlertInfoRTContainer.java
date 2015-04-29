package com.dianping.cat.report.page.storage.topology;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageAlertInfoRTContainer implements Initializable {

	public static final int SIZE = 60;

	private Map<String, Map<Long, StorageAlertInfo>> m_alertInfos = new HashMap<String, Map<Long, StorageAlertInfo>>();

	public StorageAlertInfo find(String type, long time) {
		Map<Long, StorageAlertInfo> alert = m_alertInfos.get(type);

		if (alert != null) {
			return alert.get(time);
		} else {
			return null;
		}
	}

	public Set<Long> queryExistingMinutes(String type) {
		Map<Long, StorageAlertInfo> alert = m_alertInfos.get(type);

		if (alert != null) {
			return alert.keySet();
		} else {
			return Collections.emptySet();
		}
	}

	public StorageAlertInfo findOrCreate(String type, long time) {
		Map<Long, StorageAlertInfo> alertInfo = m_alertInfos.get(type);

		if (alertInfo != null) {
			StorageAlertInfo report = alertInfo.get(time);

			if (report == null) {
				report = makeAlertInfo(type, new Date(time));
				alertInfo.put(time, report);
			}
			return report;
		} else {
			return makeAlertInfo(type, new Date(time));
		}
	}

	public boolean offer(String type, StorageAlertInfo alertInfo) {
		boolean ret = false;
		long time = alertInfo.getStartTime().getTime();
		Map<Long, StorageAlertInfo> alert = m_alertInfos.get(type);

		if (alert != null && !alert.containsKey(time)) {
			alert.put(time, alertInfo);
			ret = true;
		}
		return ret;
	}

	public StorageAlertInfo makeAlertInfo(String id, Date start) {
		StorageAlertInfo alertInfo = new StorageAlertInfo(id);

		alertInfo.setStartTime(start);
		alertInfo.setEndTime(new Date(start.getTime() + TimeHelper.ONE_MINUTE - 1));
		return alertInfo;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<Long, StorageAlertInfo> sqlAlerts = Collections.synchronizedMap(new LinkedHashMap<Long, StorageAlertInfo>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<Long, StorageAlertInfo> eldest) {
				return size() > SIZE;
			}

		});

		Map<Long, StorageAlertInfo> cacheAlerts = Collections
		      .synchronizedMap(new LinkedHashMap<Long, StorageAlertInfo>() {
			      private static final long serialVersionUID = 1L;

			      @Override
			      protected boolean removeEldestEntry(Entry<Long, StorageAlertInfo> eldest) {
				      return size() > SIZE;
			      }

		      });

		m_alertInfos.put(StorageConstants.SQL_TYPE, sqlAlerts);
		m_alertInfos.put(StorageConstants.CACHE_TYPE, cacheAlerts);
	}
}
