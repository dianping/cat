package com.dianping.cat.broker.api.app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.service.appData.entity.AppData;

public class BucketHandler implements Task, LogEnabled {

	private Logger m_logger;

	private AppDataQueue m_appDataQueue;

	private HashMap<Integer, HashMap<String, AppData>> m_mergedData;

	private long m_startTime;

	private boolean m_isActive = true;

	private static final String FILEDIRECTORY = "/data/appdatas/cat/appdata/";

	public void setActive(boolean isActive) {
		m_isActive = isActive;
	}

	public BucketHandler(long startTime) {
		m_startTime = startTime;
		m_appDataQueue = new AppDataQueue();
		m_mergedData = new LinkedHashMap<Integer, HashMap<String, AppData>>();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void run() {
		while (m_isActive) {
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

	private void processEntity(AppData appData) {
		Integer command = appData.getCommand();
		String key = m_startTime + ":" + appData.getCity() + ":" + appData.getOperator() + ":" + appData.getChannel()
		      + ":" + appData.getVersion() + ":" + appData.getNetwork() + ":" + appData.getCode() + ":"
		      + appData.getPlatform();

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

	private void end() {
		for (Entry<Integer, HashMap<String, AppData>> outerEntry : m_mergedData.entrySet()) {
			for (Entry<String, AppData> entry : outerEntry.getValue().entrySet()) {
				AppData appData = entry.getValue();

				if (toHbase(appData) == false) {
					saveToFile(appData);
				}
			}
		}
		
	}

	@Override
	public String getName() {
		return "BucketHandler";
	}

	@Override
	public void shutdown() {
		m_isActive = false;
	}

	public void enqueue(AppData appData) {
		m_appDataQueue.offer(appData);
	}

	private boolean toHbase(AppData appData) {
		return false;
	}

	private void saveToFile(AppData appData) {
		Date date = new Date();
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = formater.format(date);
		String filePath = FILEDIRECTORY + dateStr;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			String content = appData.getTimestamp() + "\t" + appData.getCity() + "\t" + appData.getOperator() + "\t"
			      + appData.getNetwork() + "\t" + appData.getVersion() + "\t" + appData.getChannel() + "\t"
			      + appData.getCommand() + "\t" + appData.getCode() + "\t" + appData.getPlatform() + "\t"
			      + appData.getRequestByte() + "\t" + appData.getResponseByte() + "\t" + appData.getResponseTime() + "\n";

			writer.append(content);
			writer.close();
		} catch (IOException e) {
			m_logger.error("save appdata to file " + filePath + " failed. " + e.getMessage());
		}
	}
}
