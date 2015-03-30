package com.dianping.cat.broker.api.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.app.bucket.BucketHandler;
import com.dianping.cat.broker.api.app.proto.ProtoData;
import com.dianping.cat.broker.api.app.service.AppService;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.message.Event;

@SuppressWarnings("rawtypes")
public class AppConsumer extends ContainerHolder implements Initializable, LogEnabled {

	public static final long MINUTE = 60 * 1000L;

	public static final long DURATION = 5 * MINUTE;

	private AppQueue m_appDataQueue;

	private volatile long m_dataLoss;

	private Logger m_logger;

	private ConcurrentHashMap<Long, BucketHandler> m_tasks;

	private Map<String, AppService> m_appDataServices;

	private AppConfigManager m_appConfigManager;

	private SimpleDateFormat m_fileFormat = new SimpleDateFormat("yyyyMMddHHmm");

	public final static String SAVE_PATH = "/data/appdatas/cat/app-data-save/";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public boolean enqueue(ProtoData appData) {
		return m_appDataQueue.offer(appData);
	}

	@Override
	public void initialize() throws InitializationException {
		m_appDataServices = lookupMap(AppService.class);
		m_appConfigManager = lookup(AppConfigManager.class);
		m_appDataQueue = new AppQueue();
		m_tasks = new ConcurrentHashMap<Long, BucketHandler>();
		AppDataDispatcherThread appDataDispatcherThread = new AppDataDispatcherThread();
		BucketThreadController bucketThreadController = new BucketThreadController();
		loadOldData();

		Threads.forGroup("cat").start(bucketThreadController);
		Threads.forGroup("cat").start(appDataDispatcherThread);
	}

	public void loadOldData() {
		File path = new File(SAVE_PATH);
		File[] files = path.listFiles();

		if (files != null && files.length > 0) {

			for (File file : files) {
				try {
					long timestamp = m_fileFormat.parse(file.getName()).getTime();
					BucketHandler handler = new BucketHandler(timestamp, m_appDataServices, m_appConfigManager);

					handler.load(file);
					Threads.forGroup("cat").start(handler);

					m_tasks.put(timestamp, handler);
				} catch (Exception e) {
					Cat.logError(e);
				} finally {
					file.delete();
				}
			}
		}

		try {
			File paths = new File(SAVE_PATH);
			File[] leftFiles = paths.listFiles();

			if (leftFiles != null && leftFiles.length > 0) {
				for (File file : leftFiles) {
					boolean success = file.delete();

					if (!success) {
						Cat.logError(new RuntimeException("error when delete file " + file.getAbsolutePath()));
					}
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void save() {
		for (Entry<Long, BucketHandler> entry : m_tasks.entrySet()) {
			BucketHandler handler = entry.getValue();

			if (!handler.isCompleted()) {
				try {
					File file = new File(SAVE_PATH + m_fileFormat.format(new Date(entry.getKey())));

					file.getParentFile().mkdirs();
					handler.save(file);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	private class AppDataDispatcherThread implements Task {

		private static final String NAME = "AppDataDispatcherThread";

		@Override
		public String getName() {
			return NAME;
		}

		private void recordErrorInfo() {
			m_dataLoss++;

			if (m_dataLoss % 1000 == 0) {
				Cat.logEvent("Discard", "BucketHandler", Event.SUCCESS, null);
				m_logger.error("error timestamp in consumer, loss:" + m_dataLoss);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					ProtoData appData = m_appDataQueue.poll();

					if (appData != null) {
						long timestamp = appData.getTimestamp();
						timestamp = timestamp - timestamp % DURATION;
						BucketHandler handler = m_tasks.get(timestamp);

						if (handler == null) {
							recordErrorInfo();
						} else {
							boolean success = handler.enqueue(appData);

							if (!success) {
								recordErrorInfo();
							}
						}
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	private class BucketThreadController implements Task {

		private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		private void closeLastTask(long currentDuration) {
			Long last = new Long(currentDuration - 2 * MINUTE - DURATION);

			for (Entry<Long, BucketHandler> entry : m_tasks.entrySet()) {
				Long time = entry.getKey();
				BucketHandler handler = entry.getValue();

				if (time <= last && handler.isActive()) {
					handler.flushReady();
					m_logger.info("closed bucket handler ,time " + m_sdf.format(new Date(time)));
				}
			}
		}

		@Override
		public String getName() {
			return "BucketThreadController";
		}

		private void removeTasks() {
			Set<Long> closed = new HashSet<Long>();

			for (Entry<Long, BucketHandler> entry : m_tasks.entrySet()) {
				Long time = entry.getKey();
				BucketHandler handler = entry.getValue();

				if (handler.isCompleted()) {
					closed.add(time);
				}
			}

			for (Long time : closed) {
				m_tasks.remove(time);
				m_logger.info("remove bucket handler ,time " + m_sdf.format(new Date(time)));
			}
		}

		@Override
		public void run() {
			while (true) {
				long curTime = System.currentTimeMillis();

				try {
					long currentDuration = curTime - curTime % DURATION;
					long currentMinute = curTime - curTime % MINUTE;

					startCurrentTask(currentDuration);
					startNextTask(currentDuration);
					closeLastTask(currentMinute);
					removeTasks();
				} catch (Exception e) {
					Cat.logError(e);
				}
				long elapsedTime = System.currentTimeMillis() - curTime;

				try {
					Thread.sleep(MINUTE - elapsedTime);
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void shutdown() {
		}

		private void startCurrentTask(long currentDuration) {
			if (m_tasks.get(currentDuration) == null) {
				BucketHandler curBucketHandler = new BucketHandler(currentDuration, m_appDataServices, m_appConfigManager);
				m_logger.info("starting bucket handler ,time " + m_sdf.format(new Date(currentDuration)));
				Threads.forGroup("cat").start(curBucketHandler);

				m_tasks.put(currentDuration, curBucketHandler);
				m_logger.info("started bucket handler ,time " + m_sdf.format(new Date(currentDuration)));
			}
		}

		private void startNextTask(long currentDuration) {
			Long next = new Long(currentDuration + DURATION);

			if (m_tasks.get(next) == null) {
				BucketHandler nextBucketHandler = new BucketHandler(next, m_appDataServices, m_appConfigManager);
				m_logger.info("starting bucket handler ,time " + m_sdf.format(new Date(next)));
				Threads.forGroup("cat").start(nextBucketHandler);

				m_tasks.put(next, nextBucketHandler);
				m_logger.info("started bucket handler ,time " + m_sdf.format(new Date(next)));
			}
		}
	}

}
