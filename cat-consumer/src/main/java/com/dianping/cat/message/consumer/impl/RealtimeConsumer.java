package com.dianping.cat.message.consumer.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

/**
 * This is the real time consumer process framework. The config of the consumer
 * contains name,domain,analyers.
 * 
 * 
 */
public class RealtimeConsumer extends ContainerHolder implements
		MessageConsumer, Initializable {
	private static final Logger LOG = Logger.getLogger(RealtimeConsumer.class);

	private static final long HOUR = 60 * 60 * 1000L;

	private static final long MINUTE = 60 * 1000L;

	private static final String DOMAIN_ALL = "all";

	private static final int PROCESS_PERIOD = 3;
	@Inject
	private String m_consumerId;

	@Inject
	private String m_domain = DOMAIN_ALL;

	@Inject
	private long m_duration = 1 * HOUR;

	@Inject
	private List<String> m_analyzerNames;

	@Inject
	private int m_threads = 10;

	private ExecutorService m_executor;

	private List<Period> m_periods = new ArrayList<Period>(PROCESS_PERIOD);

	private void cleanupQueue(List<MessageQueue> queues) {
		for (int i = queues.size() - 1; i >= 0; i--) {
			MessageQueue queue = queues.get(i);
			if (!queue.isActive()) {
				queues.remove(i);
			}
		}
	}

	private boolean isInDomain(MessageTree tree) {
		if (m_domain == null || m_domain.length() == 0
				|| m_domain.equalsIgnoreCase(DOMAIN_ALL)) {
			return true;
		}
		if (m_domain.indexOf(tree.getDomain()) > -1) {
			return true;
		}
		return false;
	}

	@Override
	public  void  consume(MessageTree tree) {
		if (!isInDomain(tree))
			return;

		long timestamp = tree.getMessage().getTimestamp();
		Period current = null;

		for (Period period : m_periods) {
			if (period.isIn(timestamp)) {
				current = period;
				break;
			}
		}

		if (current != null) {
			List<MessageQueue> queues = current.getQueues();
			boolean dirty = distributeMessage(tree, queues);
			// do clean up
			if (dirty) {
				cleanupQueue(queues);
			}
		} else {
			synchronized (this) {
				// TODO 创建任务是否需要加锁
			}
			// check the message is in the next duration
			// if not we will add many tasks
			long systemTime = System.currentTimeMillis();
			long nextStart = systemTime - systemTime % m_duration - 3
					* m_duration;
			if (timestamp < systemTime + MINUTE * 3 && timestamp >= nextStart) {
				// TODO
				startTasks(tree);
			} else {
				// TODO 建议新增接口 toString
				LOG.warn("The message is not excepceted!" + tree);
			}
		}
	}

	private boolean distributeMessage(MessageTree tree,
			List<MessageQueue> queues) {
		int size = queues.size();
		boolean dirty = false;
		// distribute to all queues
		for (int i = 0; i < size; i++) {
			MessageQueue queue = queues.get(i);
			if (queue.isActive()) {
				queue.offer(tree);
			} else {
				dirty = true;
			}
		}
		return dirty;
	}

	@Override
	public String getConsumerId() {
		return m_consumerId;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public void initialize() throws InitializationException {
		m_executor = Executors.newFixedThreadPool(m_threads);
	}

	public void setAnalyzerNames(String analyzerNames) {
		m_analyzerNames = Splitters.by(',').noEmptyItem().trim()
				.split(analyzerNames);
	}

	public void setConsumerId(String consumerId) {
		m_consumerId = consumerId;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setDuration(long duration) {
		m_duration = duration;
	}

	public void setThreads(int threads) {
		m_threads = threads;
	}

	private void startTasks(MessageTree tree) {
		long time = tree.getMessage().getTimestamp();
		long start = time - time % m_duration;
		LOG.info("Start Tasks At " + new Date(start));
		List<MessageQueue> queues = new ArrayList<MessageQueue>();
		Period current = new Period(start, start + m_duration, queues);

		for (String name : m_analyzerNames) {
			MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, name);
			//设置构造函数参数 TODO
			MessageQueue queue = lookup(MessageQueue.class);
			queue.offer(tree);
			// TODO
			queues.add(queue);
			Task task = new Task(analyzer, queue);
			m_executor.submit(task);
		}

		int len = m_periods.size();
		/*
		 * for (int i = len - 1; i >= 0; i--) { Period period =
		 * m_periods.get(i);
		 * 
		 * if (period.isIn(start - 2 * m_duration)) { m_periods.remove(i); } }
		 */
		// TODO
		if (len >= PROCESS_PERIOD)
			m_periods.remove(0);
		m_periods.add(current);
	}

	static class Period {
		private long m_startTime;

		private long m_endTime;

		private List<MessageQueue> m_queues;

		public Period(long startTime, long endTime, List<MessageQueue> queues) {
			m_startTime = startTime;
			m_endTime = endTime;
			m_queues = queues;
		}

		public List<MessageQueue> getQueues() {
			return m_queues;
		}

		public boolean isIn(long timestamp) {
			return timestamp >= m_startTime && timestamp < m_endTime;
		}
	}

	static class Task implements Runnable {
		private MessageAnalyzer m_analyzer;

		private MessageQueue m_queue;

		public Task(MessageAnalyzer analyzer, MessageQueue queue) {
			m_analyzer = analyzer;
			m_queue = queue;
		}

		public MessageQueue getQueue() {
			return m_queue;
		}

		public void run() {
			m_analyzer.analyze(m_queue);
		}
	}
}