package com.dianping.cat.consumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
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
 * contains name,domain,analyzers.
 * 
 * @author yong.you
 * @since Jan 5, 2012
 */
public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {
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
		private AnalyzerFactory m_factory;

		private MessageAnalyzer m_analyzer;

		private MessageQueue m_queue;

		public Task(AnalyzerFactory factory, MessageAnalyzer analyzer, MessageQueue queue) {
			m_factory = factory;
			m_analyzer = analyzer;
			m_queue = queue;
		}

		public MessageQueue getQueue() {
			return m_queue;
		}

		public void run() {
			m_analyzer.analyze(m_queue);
			m_factory.release(m_analyzer);
			m_factory.release(m_queue);
		}
	}

	private static final long HOUR = 60 * 60 * 1000L;

	private static final long MINUTE = 60 * 1000L;

	private static final String DOMAIN_ALL = "all";

	private static final int PROCESS_PERIOD = 3;

	private static final long FIVE_MINUTES = 5 * 60 * 1000L;

	@Inject
	private Logger m_logger;

	@Inject
	private String m_consumerId;

	@Inject
	private String m_domain = DOMAIN_ALL;

	@Inject
	private long m_duration = 1 * HOUR;

	@Inject
	private long m_extraTime = FIVE_MINUTES;
	
	@Inject
	private int m_threads = 10;

	@Inject
	private List<String> m_analyzerNames;

	@Inject
	private AnalyzerFactory m_factory;

	private ExecutorService m_executor;

	private List<Period> m_periods = new ArrayList<Period>(PROCESS_PERIOD);

	private Map<String, MessageAnalyzer> m_lastAnalyzers = new HashMap<String, MessageAnalyzer>();

	private Map<String, MessageAnalyzer> m_currentAnalyzers = new HashMap<String, MessageAnalyzer>();

	@Override
	public void consume(MessageTree tree) {
		if(!tree.getDomain().equalsIgnoreCase("Cat")){
			System.out.println(tree);
		}
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

			distributeMessage(tree, queues);
		} else {
			long now = System.currentTimeMillis();
			long nextStart = now - now % m_duration - 3 * m_duration;

			if (timestamp < now + MINUTE * 3 && timestamp >= nextStart) {
				startTasks(tree);
			} else {
				m_logger.warn("The timestamp of message is out of range, IGNORED! \r\n" + tree);
			}
		}
	}

	private void distributeMessage(MessageTree tree, List<MessageQueue> queues) {
		int size = queues.size();

		for (int i = 0; i < size; i++) {
			MessageQueue queue = queues.get(i);

			queue.offer(tree);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getConsumerId() {
		return m_consumerId;
	}

	public MessageAnalyzer getCurrentAnalyzer(String name) {
		return m_currentAnalyzers.get(name);
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	public MessageAnalyzer getLastAnalyzer(String name) {
		return m_lastAnalyzers.get(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_executor = Executors.newFixedThreadPool(m_threads);
	}

	private boolean isInDomain(MessageTree tree) {
		if (m_domain == null || m_domain.length() == 0 || m_domain.equalsIgnoreCase(DOMAIN_ALL)) {
			return true;
		}
		if (m_domain.indexOf(tree.getDomain()) > -1) {
			return true;
		}
		return false;
	}

	public void setAnalyzerNames(String analyzerNames) {
		m_analyzerNames = Splitters.by(',').noEmptyItem().trim().split(analyzerNames);
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

	public void setExtraTime(long time) {
		m_extraTime = time;
	}

	public void setFactory(AnalyzerFactory factory) {
		m_factory = factory;
	}

	public void setThreads(int threads) {
		m_threads = threads;
	}

	private void startTasks(MessageTree tree) {
		long time = tree.getMessage().getTimestamp();
		long start = time - time % m_duration;
		m_logger.info("Start Tasks At " + new Date(start));
		List<MessageQueue> queues = new ArrayList<MessageQueue>();
		Period current = new Period(start, start + m_duration, queues);

		m_lastAnalyzers.clear();
		m_lastAnalyzers.putAll(m_currentAnalyzers);
		m_currentAnalyzers.clear();

		for (String name : m_analyzerNames) {
			MessageAnalyzer analyzer = m_factory.create(name, start, m_duration, m_domain, m_extraTime);
			MessageQueue queue = lookup(MessageQueue.class);
			Task task = new Task(m_factory, analyzer, queue);

			queue.offer(tree);
			queues.add(queue);
			m_executor.submit(task);
			m_currentAnalyzers.put(name, analyzer);
		}

		int len = m_periods.size();

		if (len >= PROCESS_PERIOD) {
			m_periods.remove(0);
		}

		m_periods.add(current);
	}
}