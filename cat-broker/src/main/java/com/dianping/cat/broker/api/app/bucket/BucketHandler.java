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
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppConnectionData;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.broker.api.app.AppQueue;
import com.dianping.cat.broker.api.app.bucket.impl.ConnectionBucketExecutor;
import com.dianping.cat.broker.api.app.bucket.impl.DataBucketExecutor;
import com.dianping.cat.broker.api.app.bucket.impl.SpeedBucketExecutor;
import com.dianping.cat.broker.api.app.proto.AppConnectionProto;
import com.dianping.cat.broker.api.app.proto.AppDataProto;
import com.dianping.cat.broker.api.app.proto.AppSpeedProto;
import com.dianping.cat.broker.api.app.proto.ProtoData;
import com.dianping.cat.broker.api.app.service.AppService;
import com.dianping.cat.config.app.AppConfigManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BucketHandler implements Task {

	private boolean m_active = true;

	private boolean m_completed = false;

	private AppQueue m_appDataQueue = new AppQueue();

	private Map<String, BucketExecutor> m_bucketExecutors = new LinkedHashMap<String, BucketExecutor>();

	private long m_startTime;

	public BucketHandler(long startTime, Map<String, AppService> appDataServices, AppConfigManager appConfigManager) {
		AppService appDataCommandService = appDataServices.get(AppCommandData.class.getName());
		m_bucketExecutors.put(AppDataProto.class.getName(), new DataBucketExecutor(startTime, appDataCommandService,
		      appConfigManager));

		AppService appSpeedDataService = appDataServices.get(AppSpeedData.class.getName());
		m_bucketExecutors.put(AppSpeedProto.class.getName(), new SpeedBucketExecutor(startTime, appSpeedDataService));

		AppService appConnectionService = appDataServices.get(AppConnectionData.class.getName());
		m_bucketExecutors.put(AppConnectionProto.class.getName(), new ConnectionBucketExecutor(startTime,
		      appConnectionService, appConfigManager));
	}

	public boolean enqueue(ProtoData appData) {
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

	public AppQueue getAppDataQueue() {
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
				ProtoData appData = m_bucketExecutors.get(items[0]).loadRecord(items);

				enqueue(appData);
			}
			bufferedReader.close();
		} catch (Exception e) {
			Cat.logError(e);
		}

	}

	public void processEntity(ProtoData appData) {
		Class<? extends ProtoData> clas = appData.getClass();

		m_bucketExecutors.get(clas.getName()).processEntity(appData);
	}

	@Override
	public void run() {
		while (true) {
			try {
				ProtoData appData = m_appDataQueue.poll();

				if (appData != null) {
					processEntity(appData);
				} else if (!m_active) {
					break;
				}
			} catch (Exception e) {
				Cat.logError(e);
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
