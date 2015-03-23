package com.dianping.cat.broker.api.page;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.Constants;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;

import org.unidal.lookup.util.StringUtils;

public class MonitorManager implements Initializable, LogEnabled {

	private final int m_threadCounts = 20;

	private volatile long m_total = 0;

	private volatile long m_errorCount = -1;

	private Map<Integer, BlockingQueue<MonitorEntity>> m_queues = new LinkedHashMap<Integer, BlockingQueue<MonitorEntity>>();

	@Inject
	private IpService m_ipService;

	@Inject
	private UrlPatternConfigManager m_patternManger;

	private Logger m_logger;

	private void buildMessage(MonitorEntity entity, String url, IpInfo ipInfo) {
		String city = ipInfo.getProvince() + "-" + ipInfo.getCity();
		String channel = ipInfo.getChannel();
		String httpStatus = entity.getHttpStatus();
		String errorCode = entity.getErrorCode();
		long timestamp = entity.getTimestamp();
		double duration = entity.getDuration();
		String group = url;
		int count = entity.getCount();

		if (duration > 0) {
			logMetricForAvg(timestamp, duration, group, city + ":" + channel + ":" + Constants.AVG);
		}

		String hitKey = city + ":" + channel + ":" + Constants.HIT;

		logMetricForCount(timestamp, group, hitKey, count);

		if (!"200".equals(httpStatus)) {
			String key = city + ":" + channel + ":" + Constants.ERROR;

			logMetricForCount(timestamp, group, key, count);
		}

		if (!StringUtils.isEmpty(httpStatus)) {
			String key = city + ":" + channel + ":" + Constants.HTTP_STATUS + "|" + httpStatus;

			logMetricForCount(timestamp, group, key, count);
		}
		if (!StringUtils.isEmpty(errorCode)) {
			String key = city + ":" + channel + ":" + Constants.ERROR_CODE + "|" + errorCode;

			logMetricForCount(timestamp, group, key, count);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		for (int i = 0; i < m_threadCounts; i++) {
			BlockingQueue<MonitorEntity> queue = new LinkedBlockingQueue<MonitorEntity>(10000);
			Threads.forGroup("cat").start(new MessageSender(queue, i));

			m_queues.put(i, queue);
		}
	}

	private void logMetricForAvg(long timestamp, double duration, String group, String key) {
		Metric metric = Cat.getProducer().newMetric(group, key);
		DefaultMetric defaultMetric = (DefaultMetric) metric;

		defaultMetric.setTimestamp(timestamp);
		defaultMetric.setStatus("S,C");
		defaultMetric.addData(String.format("%s,%.2f", 1, duration));
		defaultMetric.complete();
	}

	private void logMetricForCount(long timestamp, String group, String key, int count) {
		Metric metric = Cat.getProducer().newMetric(group, key);
		DefaultMetric defaultMetric = (DefaultMetric) metric;

		defaultMetric.setTimestamp(timestamp);

		defaultMetric.setStatus("C");
		defaultMetric.addData(String.valueOf(count));
		defaultMetric.complete();
	}

	public boolean offer(MonitorEntity entity) {
		if (!StringUtils.isEmpty(entity.getTargetUrl())) {
			m_total++;

			int index = (int) (m_total % m_threadCounts);
			int retryTime = 0;

			while (retryTime < m_threadCounts) {
				BlockingQueue<MonitorEntity> queue = m_queues.get((index + retryTime) % m_threadCounts);
				boolean result = queue.offer(entity);

				if (result) {
					return true;
				}
				retryTime++;
			}

			m_errorCount++;
			if (m_errorCount % CatConstants.ERROR_COUNT == 0) {
				m_logger.error("Error when offer entity to queues, size:" + m_errorCount);
			}
		}
		return false;
	}

	private void processOneEntity(MonitorEntity entity) {
		String targetUrl = entity.getTargetUrl();
		String url = m_patternManger.handle(targetUrl);

		if (url != null) {
			Transaction t = Cat.newTransaction("Monitor", url);
			String ip = entity.getIp();
			IpInfo ipInfo = m_ipService.findIpInfoByString(ip);

			try {
				if (ipInfo != null) {
					buildMessage(entity, url, ipInfo);
				} else {
					Cat.logEvent("ip", "notFound", Event.SUCCESS, ip);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}
		}
	}

	public class MessageSender implements Task {

		private BlockingQueue<MonitorEntity> m_queue;

		private int m_index;

		public MessageSender(BlockingQueue<MonitorEntity> queue, int index) {
			m_queue = queue;
			m_index = index;
		}

		@Override
		public String getName() {
			return "Message-Send-" + m_index;
		}

		@Override
		public void run() {
			while (true) {
				try {
					MonitorEntity entity = m_queue.poll(5, TimeUnit.MILLISECONDS);

					if (entity != null) {
						try {
							processOneEntity(entity);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
