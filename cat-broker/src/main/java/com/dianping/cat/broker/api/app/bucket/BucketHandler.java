package com.dianping.cat.broker.api.app.bucket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.app.AppData;
import com.dianping.cat.broker.api.app.AppDataQueue;
import com.dianping.cat.broker.api.app.AppDataType;
import com.dianping.cat.config.app.AppDataService;

public class BucketHandler implements Task {

	private boolean m_active = true;

	private boolean m_completed = false;

	private AppDataQueue m_appDataQueue = new AppDataQueue();

	private HashMap<AppDataType, BucketExecutor> m_bucketExecutors = new LinkedHashMap<AppDataType, BucketExecutor>();

	private long m_startTime;

	public BucketHandler(long startTime, AppDataService appDataService) {
		m_bucketExecutors.put(AppDataType.COMMAND, new CommandDataExecutor(startTime, appDataService));
		m_bucketExecutors.put(AppDataType.CRASH, new CrashDataExecutor());
	}

	public boolean enqueue(AppData appData) {
		return m_appDataQueue.offer(appData);
	}

	public void flush() {
		for (BucketExecutor bucketExecutor : m_bucketExecutors.values()) {
			bucketExecutor.flush();
		}
		m_completed = true;
	}

	public void flushReady() {
		m_active = false;
	}

	public AppDataQueue getAppDataQueue() {
		return m_appDataQueue;
	}

	public HashMap<AppDataType, BucketExecutor> getBucketExecutors() {
		return m_bucketExecutors;
	}

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		return "BucketHandler-" + sdf.format(new Date(m_startTime));
	}

	public long getStartTime() {
		return m_startTime;
	}

	public boolean isActive() {
		return m_active;
	}

	public boolean isCompleted() {
		return m_completed;
	}

	public void load(File file) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				String[] items = line.split("\t");

				AppDataType type = AppDataType.getByName(items[0], AppDataType.COMMAND);
				AppData appData = m_bucketExecutors.get(type).loadRecord(items, type);

				enqueue(appData);
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError(e);
		}
	}

	public void processEntity(AppData appData) {
		AppDataType type = appData.getType();

		m_bucketExecutors.get(type).processEntity(appData);
	}

	@Override
	public void run() {
		while (true) {
			AppData appData = m_appDataQueue.poll();

			if (appData != null) {
				processEntity(appData);
			} else if (!m_active) {
				break;
			}
		}

		flush();
	}

	public void save(File file) {
		for (BucketExecutor bucketExecutor : m_bucketExecutors.values()) {
			bucketExecutor.save(file);
		}
	}

	@Override
	public void shutdown() {
	}
}
