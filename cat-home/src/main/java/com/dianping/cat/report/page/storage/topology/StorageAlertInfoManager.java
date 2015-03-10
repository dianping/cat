package com.dianping.cat.report.page.storage.topology;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.page.storage.Model;
import com.dianping.cat.report.page.storage.Payload;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageAlertInfoManager implements Initializable {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private StorageGraphBuilder m_builder;

	@Inject
	private StorageAlertInfoRTContainer m_alertInfoRTContainer;

	public static final int DEFAULT_MINUTE_COUNT = 8;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private StorageAlertInfo buildFromDatabase(long time, int minute) {
		Date start = new Date(time + minute * TimeHelper.ONE_MINUTE);
		Date end = new Date(start.getTime() + TimeHelper.ONE_MINUTE - 1000);
		StorageAlertInfo alertInfo = m_alertInfoRTContainer.makeAlertInfo(StorageConstants.SQL_TYPE, start);

		try {
			List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(start, end, AlertType.StorageDatabase.getName(),
			      AlertEntity.READSET_FULL);

			for (Alert alert : alerts) {
				m_builder.parseAlertEntity(alert, alertInfo);
			}
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return alertInfo;
	}

	public StorageAlertInfo queryAlertInfo(long time, int minute, int tops) {
		StorageAlertInfo alertInfo = m_alertInfoRTContainer.find(time, minute);

		if (alertInfo == null) {
			alertInfo = buildFromDatabase(time, minute);
		}
		List<Entry<String, Storage>> tmp = new ArrayList<Entry<String, Storage>>(alertInfo.getStorages().entrySet());

		Collections.sort(tmp, new AlertInfoStorageComparator());

		if (tmp.size() > tops) {
			tmp = tmp.subList(0, tops);
		}
		StorageAlertInfo result = m_alertInfoRTContainer.makeAlertInfo(alertInfo.getId(), alertInfo.getStartTime());
		Map<String, Storage> storages = result.getStorages();

		for (Entry<String, Storage> storage : tmp) {
			storages.put(storage.getKey(), storage.getValue());
		}
		return result;
	}

	private Map<String, StorageAlertInfo> queryAlertInfos(long time, int start, int end, int tops) {
		Map<String, StorageAlertInfo> alertInfos = new LinkedHashMap<String, StorageAlertInfo>();

		for (int i = start; i <= end; i++) {
			StorageAlertInfo alertInfo = queryAlertInfo(time, i, tops);
			Date minute = new Date(time + i * TimeHelper.ONE_MINUTE);

			alertInfos.put(m_sdf.format(minute), alertInfo);
		}
		return alertInfos;
	}

	public Map<String, StorageAlertInfo> queryAlertInfos(Payload payload, Model model) {
		long time = payload.getDate();
		int minute = model.getMinute();
		int count = payload.getMinuteCounts();
		int tops = payload.getTopCounts();
		Map<String, StorageAlertInfo> alertInfos = new LinkedHashMap<String, StorageAlertInfo>();
		int start = minute - count + 1;

		if (start < 0) {
			start = 0;
			int lastStart = 60 - (count - minute - 1);

			alertInfos.putAll(queryAlertInfos(time - TimeHelper.ONE_HOUR, lastStart, 59, tops));
		}

		alertInfos.putAll(queryAlertInfos(time, start, minute, tops));

		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();
		List<String> keys = new ArrayList<String>(alertInfos.keySet());

		Collections.sort(keys, new StringCompartor());

		for (String key : keys) {
			results.put(key, alertInfos.get(key));
		}
		return results;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isAlertMachine()) {
			Threads.forGroup("cat").start(new StorageAlerInfoLoadTask());
		}
	}

	public class StorageAlerInfoLoadTask implements Task {

		@Override
		public void run() {
			long endTime = TimeHelper.getCurrentMinute().getTime() - TimeHelper.ONE_MINUTE;
			long startTime = endTime - TimeHelper.ONE_MINUTE * DEFAULT_MINUTE_COUNT;

			for (long current = startTime; current <= endTime; current += TimeHelper.ONE_MINUTE) {
				try {
					Date start = new Date(current);
					Date end = new Date(current + TimeHelper.ONE_MINUTE - 1000);
					StorageAlertInfo alertInfo = m_alertInfoRTContainer.makeAlertInfo(StorageConstants.SQL_TYPE, start);
					List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(start, end,
					      AlertType.StorageDatabase.getName(), AlertEntity.READSET_FULL);

					for (Alert alert : alerts) {
						m_builder.parseAlertEntity(alert, alertInfo);
					}
					m_alertInfoRTContainer.offer(alertInfo);
				} catch (DalNotFoundException e) {
					// ignore
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public String getName() {
			return "storage-alertInfo-load-task";
		}

		@Override
		public void shutdown() {
		}
	}

	public static class AlertInfoStorageComparator implements Comparator<Entry<String, Storage>> {

		@Override
		public int compare(Entry<String, Storage> o1, Entry<String, Storage> o2) {
			int gap = o2.getValue().getLevel() - o1.getValue().getLevel();

			return gap == 0 ? o2.getValue().getCount() - o1.getValue().getCount() : gap;
		}

	}

	public static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String hour1 = o1.substring(0, 2);
			String hour2 = o2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				int hour1Value = Integer.parseInt(hour1);
				int hour2Value = Integer.parseInt(hour2);

				if (hour1Value == 0 && hour2Value == 23) {
					return -1;
				} else if (hour1Value == 23 && hour2Value == 0) {
					return 1;
				} else {
					return hour2Value - hour1Value;
				}
			} else {
				String first = o1.substring(3, 5);
				String end = o2.substring(3, 5);

				return Integer.parseInt(end) - Integer.parseInt(first);
			}
		}
	}

}
