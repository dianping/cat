package com.dianping.cat.consumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

/**
 * This is the real time consumer process framework. All analyzers share the
 * message decoding once, thereof reduce the overhead.
 * <p>
 * 
 * @author yong.you
 * @since Jan 5, 2012
 */
public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {
	private static final long HOUR = 60 * 60 * 1000L;

	private static final long MINUTE = 60 * 1000L;

	private static final int PROCESS_PERIOD = 3;

	private static final long FIVE_MINUTES = 5 * 60 * 1000L;

	@Inject
	private Logger m_logger;

	@Inject
	private String m_consumerId;

	@Inject
	private List<String> m_eligibleDomains; // domains == null means not limit

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

	private Set<String> m_domains = new HashSet<String>();

	@Override
	public void consume(MessageTree tree) {
		if (!isInDomain(tree)) {
			return;
		}

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

		m_domains.add(tree.getDomain());
	}

	private void distributeMessage(MessageTree tree, List<MessageQueue> queues) {
		int size = queues.size();

		for (int i = 0; i < size; i++) {
			MessageQueue queue = queues.get(i);

			queue.offer(tree);
		}
	}

	public void doCheckpoint() throws IOException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);

		try {
			for (Map.Entry<String, MessageAnalyzer> e : m_currentAnalyzers.entrySet()) {
				e.getValue().doCheckpoint();
			}
		} catch (IOException e) {
			cat.logError(e);
			t.setStatus(e);
		} catch (RuntimeException e) {
			cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
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

	public MessageAnalyzer getLastAnalyzer(String name) {
		return m_lastAnalyzers.get(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_executor = Executors.newFixedThreadPool(m_threads);
	}

	private boolean isInDomain(MessageTree tree) {
		if (m_eligibleDomains == null || m_eligibleDomains.isEmpty()) {
			return true;
		} else {
			return m_eligibleDomains.contains(tree.getDomain());
		}
	}

	public void setAnalyzers(String analyzers) {
		m_analyzerNames = Splitters.by(',').noEmptyItem().trim().split(analyzers);
	}

	public void setConsumerId(String consumerId) {
		m_consumerId = consumerId;
	}

	public void setDomains(String domains) {
		if (domains != null) {
			m_eligibleDomains = Splitters.by(',').noEmptyItem().trim().split(domains);
		}
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
		CountDownLatch latch = new CountDownLatch(m_analyzerNames.size());

		m_lastAnalyzers.clear();
		m_lastAnalyzers.putAll(m_currentAnalyzers);
		m_currentAnalyzers.clear();

		Cat.setup("realtime-consumer");

		for (String name : m_analyzerNames) {
			MessageAnalyzer analyzer = m_factory.create(name, start, m_duration, m_extraTime);
			MessageQueue queue = lookup(MessageQueue.class);
			Task task = new Task(m_factory, analyzer, queue, latch);

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

	static class FinalizerTask implements Runnable {
		private AnalyzerFactory m_factory;

		private MessageAnalyzer m_handler;

		private long m_duration;

		private CountDownLatch m_latch;

		public FinalizerTask(AnalyzerFactory factory, MessageAnalyzer handler, long duration, CountDownLatch latch) {
			m_factory = factory;
			m_handler = handler;
			m_duration = duration;
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				m_latch.await(m_duration * 2, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.println("Waiting time out in FinalizerTask, do logview upload next");
			}

			try {
				m_handler.doCheckpoint();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				m_factory.release(m_handler);
			}
		}
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
		private AnalyzerFactory m_factory;

		private MessageAnalyzer m_analyzer;

		private MessageQueue m_queue;

		private CountDownLatch m_latch;

		public Task(AnalyzerFactory factory, MessageAnalyzer analyzer, MessageQueue queue, CountDownLatch latch) {
			m_factory = factory;
			m_analyzer = analyzer;
			m_queue = queue;
			m_latch = latch;
		}

		public MessageQueue getQueue() {
			return m_queue;
		}

		public void run() {
			Cat.setup("realtime-consumer-task");

			try {
				m_analyzer.analyze(m_queue);
				m_factory.release(m_analyzer);
				m_factory.release(m_queue);
				m_latch.countDown();
			} finally {
				Cat.reset();
			}
		}
	}
}