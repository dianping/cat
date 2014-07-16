package com.dianping.cat.broker.api.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppDataService;

public class BucketHandler implements Task {

	private static final String FILEDIRECTORY = "/data/appdatas/cat/app/";

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
		for (Entry<Integer, HashMap<String, AppData>> outerEntry : m_mergedData.entrySet()) {
			for (Entry<String, AppData> entry : outerEntry.getValue().entrySet()) {
				AppData appData = entry.getValue();

				saveToDataBase(appData);
			}
		}
	}

	public void enqueue(AppData appData) {
		m_appDataQueue.offer(appData);
	}

	@Override
	public String getName() {
		return "BucketHandler";
	}

	public boolean isActive() {
		synchronized (this) {
			return m_isActive;
		}
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
				processEntity(appData);
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

	private void saveToDataBase(AppData appData) {
		int minute = (int) (m_startTime % ONE_DAY / ONE_MINUTE);
		Date period = new Date(m_startTime - minute * ONE_MINUTE);

		try {
			m_appDataService.insert(period, minute, appData.getCommand(), appData.getCity(), appData.getOperator(),
			      appData.getNetwork(), appData.getVersion(), appData.getConnectType(), appData.getCode(),
			      appData.getPlatform(), appData.getCount(), appData.getResponseTime(), appData.getResponseByte(),
			      appData.getResponseByte());
		} catch (Exception e) {
			Cat.logError(e);

			saveToFile(appData);
		}
	}

	private void saveToFile(AppData appData) {
		Date date = new Date();
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = formater.format(date);
		String filePath = FILEDIRECTORY + dateStr;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			String content = appData.getTimestamp() + "\t" + appData.getCity() + "\t" + appData.getOperator() + "\t"
			      + appData.getNetwork() + "\t" + appData.getVersion() + "\t" + appData.getConnectType() + "\t"
			      + appData.getCommand() + "\t" + appData.getCode() + "\t" + appData.getPlatform() + "\t"
			      + appData.getRequestByte() + "\t" + appData.getResponseByte() + "\t" + appData.getResponseTime() + "\n";

			writer.append(content);
			writer.close();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public void shutdown() {
		synchronized (this) {
			m_isActive = false;
		}
	}

}
