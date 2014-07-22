package com.dianping.cat.broker.api.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.config.app.AppDataService;

public class BucketHandler implements Task {

	private static int ONE_MINUTE = 60 * 1000;

	private static int ONE_DAY = 24 * 60 * ONE_MINUTE;

	private AppDataQueue m_appDataQueue;

	private AppDataService m_appDataService;

	private boolean m_isActive = true;

	private HashMap<Integer, HashMap<String, AppData>> m_mergedData;

	private long m_startTime;

	public BucketHandler(long startTime, AppDataService appDataService) {
		m_startTime = startTime;
		m_appDataQueue = new AppDataQueue();
		m_mergedData = new LinkedHashMap<Integer, HashMap<String, AppData>>();
		m_appDataService = appDataService;
	}

	private void end() {
		int minute = (int) (m_startTime % ONE_DAY / ONE_MINUTE);
		Date period = new Date(m_startTime - minute * ONE_MINUTE);
		List<AppDataCommand> appDataCommands = new ArrayList<AppDataCommand>();
		int batchSize = 100;

		for (Entry<Integer, HashMap<String, AppData>> outerEntry : m_mergedData.entrySet()) {
			HashMap<String, AppData> value = outerEntry.getValue();
			
			for (Entry<String, AppData> entry : value.entrySet()) {
				AppData appData = entry.getValue();
				AppDataCommand proto = new AppDataCommand();

				proto.setPeriod(period);
				proto.setMinuteOrder(minute);
				proto.setCommandId(appData.getCommand());
				proto.setCity(appData.getCity());
				proto.setOperator(appData.getOperator());
				proto.setNetwork(appData.getNetwork());
				proto.setAppVersion(appData.getVersion());
				proto.setConnnectType(appData.getConnectType());
				proto.setCode(appData.getCode());
				proto.setPlatform(appData.getPlatform());
				proto.setAccessNumber(appData.getCount());
				proto.setResponseSumTime(appData.getResponseTime());
				proto.setRequestPackage(appData.getRequestByte());
				proto.setResponsePackage(appData.getResponseByte());
				proto.setCreationDate(new Date());
				appDataCommands.add(proto);

				if (appDataCommands.size() >= batchSize) {
					batchInsert(appDataCommands);
					appDataCommands = new ArrayList<AppDataCommand>();
				}
			}
		}
		batchInsert(appDataCommands);
	}

	private void batchInsert(List<AppDataCommand> appDataCommands) {
		try {
			int length = appDataCommands.size();
			AppDataCommand[] array = new AppDataCommand[length];

			for (int i = 0; i < length; i++) {
				array[i] = appDataCommands.get(i);
			}

			m_appDataService.insert(array);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void enqueue(AppData appData) {
		m_appDataQueue.offer(appData);
	}

	@Override
	public String getName() {
		return "BucketHandler-" + m_startTime;
	}

	public boolean isActive() {
		return m_isActive;
	}

	private void processEntity(AppData appData) {
		int command = appData.getCommand();
		StringBuilder sb = new StringBuilder();

		sb.append(m_startTime).append(":");
		sb.append(appData.getCity()).append(":");
		sb.append(appData.getOperator()).append(":");
		sb.append(appData.getConnectType()).append(":");
		sb.append(appData.getVersion()).append(":");
		sb.append(appData.getNetwork()).append(":");
		sb.append(appData.getCode()).append(":");
		sb.append(appData.getPlatform());

		String key = sb.toString();
		HashMap<String, AppData> secondMap = m_mergedData.get(command);

		if (secondMap == null) {
			secondMap = new LinkedHashMap<String, AppData>();

			secondMap.put(key, appData);
			m_mergedData.put(command, secondMap);
		} else {
			AppData mergedAppData = secondMap.get(key);

			if (mergedAppData == null) {
				secondMap.put(key, appData);
			} else {
				mergedAppData.setCount(mergedAppData.getCount() + 1);
				mergedAppData.setRequestByte(mergedAppData.getRequestByte() + appData.getRequestByte());
				mergedAppData.setResponseByte(mergedAppData.getResponseByte() + appData.getResponseByte());
				mergedAppData.setResponseTime(mergedAppData.getResponseTime() + appData.getResponseTime());
			}
		}
	}

	@Override
	public void run() {
		while (isActive()) {
			AppData appData = m_appDataQueue.poll();

			if (appData != null) {
				try {
					processEntity(appData);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		while (true) {
			AppData appData = m_appDataQueue.poll();

			if (appData != null) {
				processEntity(appData);
			} else {
				break;
			}
		}

		end();
	}

	@Override
	public void shutdown() {
		m_isActive = false;
	}

}
