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
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.message.Transaction;
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

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private Map<String, StorageAlertInfo> convertAlertInfo(Map<Long, StorageAlertInfo> alertInfos, int tops) {
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();

		for (Entry<Long, StorageAlertInfo> alertInfo : alertInfos.entrySet()) {
			StorageAlertInfo alert = alertInfo.getValue();
			List<Entry<String, Storage>> tmp = new ArrayList<Entry<String, Storage>>(alert.getStorages().entrySet());

			Collections.sort(tmp, new AlertInfoStorageComparator());

			if (tmp.size() > tops) {
				tmp = tmp.subList(0, tops);
			}

			StorageAlertInfo result = m_alertInfoRTContainer.makeAlertInfo(alert.getId(), alert.getStartTime());
			Map<String, Storage> storages = result.getStorages();

			for (Entry<String, Storage> storage : tmp) {
				storages.put(storage.getKey(), storage.getValue());
			}
			results.put(m_sdf.format(new Date(alertInfo.getKey())), result);
		}
		return results;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isAlertMachine()) {
			Threads.forGroup("cat").start(new StorageAlerInfoLoadTask());
		}
	}

	private Map<String, StorageAlertInfo> queryAlertInfos(long start, long end, int tops) {
		Map<Long, StorageAlertInfo> alertInfos = new LinkedHashMap<Long, StorageAlertInfo>();
		Pair<Map<Long, StorageAlertInfo>, List<Long>> pair = m_alertInfoRTContainer.find(start, end);
		List<Long> historyTimes = pair.getValue();

		alertInfos.putAll(pair.getKey());

		if (historyTimes.size() > 0) {
			Date historyStart = new Date(historyTimes.get(0));
			Date historyEnd = new Date(historyTimes.get(historyTimes.size() - 1) + TimeHelper.ONE_MINUTE - 1000);

			try {
				List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(historyStart, historyEnd,
				      AlertType.STORAGE_SQL.getName(), AlertEntity.READSET_FULL);

				alertInfos.putAll(m_builder.buildStorageAlertInfos(alerts));
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return convertAlertInfo(alertInfos, tops);
	}

	public Map<String, StorageAlertInfo> queryAlertInfos(Payload payload, Model model) {
		long end = payload.getDate() + model.getMinute() * TimeHelper.ONE_MINUTE;
		long start = end - (payload.getMinuteCounts() - 1) * TimeHelper.ONE_MINUTE;

		Map<String, StorageAlertInfo> alertInfos = queryAlertInfos(start, end, payload.getTopCounts());
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();
		List<String> keys = new ArrayList<String>(alertInfos.keySet());

		Collections.sort(keys, new StringCompartor());

		for (String key : keys) {
			results.put(key, alertInfos.get(key));
		}
		return results;
	}

	public static class AlertInfoStorageComparator implements Comparator<Entry<String, Storage>> {

		@Override
		public int compare(Entry<String, Storage> o1, Entry<String, Storage> o2) {
			int gap = o2.getValue().getLevel() - o1.getValue().getLevel();

			return gap == 0 ? o2.getValue().getCount() - o1.getValue().getCount() : gap;
		}

	}

	public class StorageAlerInfoLoadTask implements Task {

		@Override
		public String getName() {
			return "storage-alertInfo-load-task";
		}

		@Override
		public void run() {
			long endTime = TimeHelper.getCurrentMinute().getTime() - TimeHelper.ONE_MINUTE;
			long startTime = endTime - TimeHelper.ONE_MINUTE * StorageConstants.DEFAULT_MINUTE_COUNT;
			Transaction t = Cat.newTransaction("ReloadTask", "StorageAlertRecover");

			try {
				for (long current = startTime; current <= endTime; current += TimeHelper.ONE_MINUTE) {
					try {
						Date start = new Date(current);
						Date end = new Date(current + TimeHelper.ONE_MINUTE - 1000);
						StorageAlertInfo alertInfo = m_alertInfoRTContainer.makeAlertInfo(StorageConstants.SQL_TYPE, start);
						List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(start, end,
						      AlertType.STORAGE_SQL.getName(), AlertEntity.READSET_FULL);

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
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}

		@Override
		public void shutdown() {
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
