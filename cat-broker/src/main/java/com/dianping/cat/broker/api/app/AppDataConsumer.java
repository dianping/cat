package com.dianping.cat.broker.api.app;

import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppDataService;

public class AppDataConsumer implements Initializable, LogEnabled {

	public static final long DURATION = 5 * 60 * 1000L;
	
	@Inject
	private AppDataService m_appDataService;

	private AppDataQueue m_appDataQueue;

	private long m_dataLoss;

	private Logger m_logger;

	private ConcurrentHashMap<Long, BucketHandler> m_tasks;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void enqueue(AppData appData) {
		m_appDataQueue.offer(appData);
	}

	public long getDataLoss() {
		return m_dataLoss;
	}

	@Override
	public void initialize() throws InitializationException {
		m_appDataQueue = new AppDataQueue();
		m_tasks = new ConcurrentHashMap<Long, BucketHandler>();
		AppDataDispatcherThread appDataDispatcherThread = new AppDataDispatcherThread();
		BucketThreadController bucketThreadController = new BucketThreadController();

		Threads.forGroup("Cat").start(bucketThreadController);
		Threads.forGroup("Cat").start(appDataDispatcherThread);
	}

	public void setDataLoss(long dataLoss) {
		m_dataLoss = dataLoss;
	}

	private class AppDataDispatcherThread implements Task {

		private static final String NAME = "AppDataDispatcherThread";

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public void run() {
			while (true) {
				try {
					AppData appData = m_appDataQueue.poll();

					if (appData != null) {
						long timestamp = appData.getTimestamp();
						timestamp -= timestamp % DURATION;
						BucketHandler handler = m_tasks.get(new Long(timestamp));

						if (handler == null || !handler.isActive()) {
							m_dataLoss++;

							if (m_dataLoss % 1000 == 0) {
								m_logger.error("error timestamp in consumer, loss:" + m_dataLoss);
							}
						} else {
							handler.enqueue(appData);
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

		private void closeLastTask(long currentDuration) {
			Long last = new Long(currentDuration - DURATION);
			BucketHandler lastBucketHandler = m_tasks.get(last);

			if (lastBucketHandler != null) {
				lastBucketHandler.shutdown();
			}
		}

		@Override
		public String getName() {
			return "BucketThreadController";
		}

		private void removeLastLastTask(long currentDuration) {
			Long lastLast = new Long(currentDuration - 2 * DURATION);

			m_tasks.remove(lastLast);
		}

		@Override
		public void run() {
			while (true) {
				long curTime = System.currentTimeMillis();

				try {
					long currentDuration = curTime - curTime % DURATION;
					
					removeLastLastTask(currentDuration);
					closeLastTask(currentDuration);
					startCurrentTask(currentDuration);
					startNextTask(currentDuration);
				} catch (Exception e) {
					Cat.logError(e);
				}
				long elapsedTime = System.currentTimeMillis() - curTime;

				try {
					Thread.sleep(DURATION - elapsedTime);
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void shutdown() {
		}

		private void startCurrentTask(long currentDuration) {
			Long cur = new Long(currentDuration);
			if (m_tasks.get(cur) == null) {
				BucketHandler curBucketHandler = new BucketHandler(cur, m_appDataService);
				Threads.forGroup("Cat").start(curBucketHandler);

				m_tasks.put(cur, curBucketHandler);
			}
		}

		private void startNextTask(long currentDuration) {
			Long next = new Long(currentDuration + DURATION);
			if (m_tasks.get(next) == null) {
				BucketHandler nextBucketHandler = new BucketHandler(next, m_appDataService);
				Threads.forGroup("Cat").start(nextBucketHandler);

				m_tasks.put(next, nextBucketHandler);
			}
		}
	}
	
}
