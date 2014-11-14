package com.dianping.cat.broker.api.app.bucket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.broker.api.app.AppCommandData;
import com.dianping.cat.broker.api.app.AppDataQueue;
import com.dianping.cat.broker.api.app.BaseData;
import com.dianping.cat.broker.api.app.RawAppSpeedData;
import com.dianping.cat.service.app.BaseAppDataService;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BucketHandler implements Task {

	private boolean m_active = true;

	private boolean m_completed = false;

	private AppDataQueue m_appDataQueue = new AppDataQueue();

	private Map<String, BucketExecutor> m_bucketExecutors = new LinkedHashMap<String, BucketExecutor>();

	private long m_startTime;

	public BucketHandler(long startTime, Map<String, BaseAppDataService> appDataServices) {
		BaseAppDataService appDataCommandService = appDataServices.get(AppDataCommand.class.getName());
		m_bucketExecutors
		      .put(AppCommandData.class.getName(), new CommandBucketExecutor(startTime, appDataCommandService));
		BaseAppDataService appSpeedDataService = appDataServices.get(AppSpeedData.class.getName());
		m_bucketExecutors.put(RawAppSpeedData.class.getName(), new SpeedBucketExecutor(startTime, appSpeedDataService));
	}

	public boolean enqueue(BaseData appData) {
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

	public Map<String, BucketExecutor> getBucketExecutors() {
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
				BaseData appData = m_bucketExecutors.get(items[0]).loadRecord(items);

				enqueue(appData);
			}
			bufferedReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}

	}

	public void processEntity(BaseData appData) {
		Class<? extends BaseData> clas = appData.getClass();

		m_bucketExecutors.get(clas.getName()).processEntity(appData);
	}

	@Override
	public void run() {
		while (true) {
			BaseData appData = m_appDataQueue.poll();

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
