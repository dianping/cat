package com.dianping.cat.report.page.storage.topology;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Alert;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertEntity;
import com.dianping.cat.home.storage.alert.entity.Storage;
import com.dianping.cat.home.storage.alert.entity.StorageAlertInfo;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.storage.Model;
import com.dianping.cat.report.page.storage.Payload;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageAlertInfoManager implements Initializable {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	@Inject
	private AlertDao m_alertDao;

	@Inject
	private StorageAlertInfoBuilder m_builder;

	private StorageAlertInfoRTContainer m_alertInfoRTContainer;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private void checkAlertInfos(Map<String, StorageAlertInfo> results, long start, long end, Payload payload) {
		if (results.size() < payload.getMinuteCounts()) {
			for (long s = start; s <= end; s += TimeHelper.ONE_MINUTE) {
				String title = m_sdf.format(new Date(s));

				if (!results.containsKey(title)) {
					StorageAlertInfo blankAlertInfo = new StorageAlertInfo(payload.getType());

					results.put(title, blankAlertInfo);
				}
			}
		}
	}

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

	private Map<String, StorageAlertInfo> queryAlertInfos(long start, long end, int tops, String type) {
		Map<Long, StorageAlertInfo> alertInfos = new LinkedHashMap<Long, StorageAlertInfo>();
		Pair<Map<Long, StorageAlertInfo>, List<Long>> pair = queryFromMemory(start, end, type);
		List<Long> historyTimes = pair.getValue();

		alertInfos.putAll(pair.getKey());

		if (historyTimes.size() > 0) {
			Date historyStart = new Date(historyTimes.get(0) + TimeHelper.ONE_MINUTE);
			Date historyEnd = new Date(historyTimes.get(historyTimes.size() - 1) + 2 * TimeHelper.ONE_MINUTE
			      - TimeHelper.ONE_SECOND);

			try {
				List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(historyStart, historyEnd, type,
				      AlertEntity.READSET_FULL);

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
		int minuteCounts = payload.getMinuteCounts();
		long end = payload.getDate() + model.getMinute() * TimeHelper.ONE_MINUTE;
		long start = end - (minuteCounts - 1) * TimeHelper.ONE_MINUTE;
		Map<String, StorageAlertInfo> results = new LinkedHashMap<String, StorageAlertInfo>();
		Map<String, StorageAlertInfo> alertInfos = queryAlertInfos(start, end, payload.getTopCounts(), payload.getType());

		checkAlertInfos(alertInfos, start, end, payload);

		List<String> keys = new ArrayList<String>(alertInfos.keySet());
		Collections.sort(keys, new StringCompartor());

		for (String key : keys) {
			results.put(key, alertInfos.get(key));
		}
		return results;
	}

	public Pair<Map<Long, StorageAlertInfo>, List<Long>> queryFromMemory(long start, long end, String type) {
		Map<Long, StorageAlertInfo> alertInfos = new LinkedHashMap<Long, StorageAlertInfo>();
		List<Long> historyMinutes = new LinkedList<Long>();
		Set<Long> timeKeys = m_alertInfoRTContainer.queryExistingMinutes(type);
		long earliest = Long.MAX_VALUE;

		if (!timeKeys.isEmpty()) {
			earliest = Collections.min(timeKeys);
		}

		for (long s = start; s <= end; s += TimeHelper.ONE_MINUTE) {
			StorageAlertInfo alertInfo = m_alertInfoRTContainer.find(type, s);

			if (alertInfo != null) {
				alertInfos.put(s, alertInfo);
			} else {
				if (s < earliest) {
					historyMinutes.add(s);
				}
			}
		}
		return new Pair<Map<Long, StorageAlertInfo>, List<Long>>(alertInfos, historyMinutes);
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
			long current = TimeHelper.getCurrentMinute().getTime();
			Date start = new Date(current + (1 - StorageConstants.DEFAULT_MINUTE_COUNT) * TimeHelper.ONE_MINUTE);
			Date end = new Date(current + TimeHelper.ONE_MINUTE - TimeHelper.ONE_SECOND);
			Transaction t = Cat.newTransaction("ReloadTask", "StorageAlertRecover");

			try {
				try {
					buildAlertInfos(start, end, StorageConstants.SQL_TYPE);
					buildAlertInfos(start, end, StorageConstants.CACHE_TYPE);
				} catch (DalNotFoundException e) {
					// ignore
				} catch (Exception e) {
					Cat.logError(e);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}

		private void buildAlertInfos(Date start, Date end, String type) throws DalException {
			List<Alert> alerts = m_alertDao.queryAlertsByTimeCategory(start, end, type, AlertEntity.READSET_FULL);
			Map<Long, StorageAlertInfo> alertInfos = m_builder.buildStorageAlertInfos(alerts);

			for (Entry<Long, StorageAlertInfo> entry : alertInfos.entrySet()) {
				m_alertInfoRTContainer.offer(type, entry.getValue());
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
