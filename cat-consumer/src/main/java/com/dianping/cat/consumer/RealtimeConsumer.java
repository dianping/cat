package com.dianping.cat.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.logview.LogviewUploader;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
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
	private static final long MINUTE = 60 * 1000L;

	private static final long FIVE_MINUTES = 5 * MINUTE;

	private static final long HOUR = 60 * MINUTE;

	@Inject
	private AnalyzerFactory m_factory;

	@Inject
	private LogviewUploader m_uploader;

	@Inject
	private long m_duration = 1 * HOUR;

	@Inject
	private long m_extraTime = FIVE_MINUTES;

	@Inject
	private int m_threads = 20;

	@Inject
	private List<String> m_analyzerNames;

	private ExecutorService m_executor;

	private Set<String> m_domains = new HashSet<String>();

	private Logger m_logger;

	private PeriodManager m_periodManager;

	@Override
	public void consume(MessageTree tree) {
		long timestamp = tree.getMessage().getTimestamp();
		Period period = m_periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);

			String domain = tree.getDomain();

			if (!m_domains.contains(domain)) {
				m_domains.add(domain);
			}
		} else {
			m_logger.warn("The timestamp of message is out of range, IGNORED! \r\n" + tree);
		}
	}

	public void doCheckpoint() throws IOException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			long currentStartTime = m_periodManager.getCurrentStartTime();
			Period period = m_periodManager.findPeriod(currentStartTime);

			for (MessageAnalyzer analyzer : period.getAnalzyers()) {
				analyzer.doCheckpoint(false);
			}

			t.setStatus(Message.SUCCESS);
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
		return "realtime";
	}

	public MessageAnalyzer getCurrentAnalyzer(String name) {
		long currentStartTime = m_periodManager.getCurrentStartTime();
		Period period = m_periodManager.findPeriod(currentStartTime);

		return period.getAnalyzer(name);
	}

	public MessageAnalyzer getLastAnalyzer(String name) {
		long lastStartTime = m_periodManager.getCurrentStartTime() - m_duration;
		Period period = m_periodManager.findPeriod(lastStartTime);

		return period == null ? null : period.getAnalyzer(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_executor = Executors.newFixedThreadPool(m_threads);
		m_periodManager = new PeriodManager(m_duration);

		m_periodManager.setName("RealtimeConsumer-PeriodManager");
		m_periodManager.start();

		if (m_uploader != null && !m_uploader.isLocalMode()) {
			Thread uploadThread = new Thread(m_uploader);

			uploadThread.setName("LogviewUploader");
			uploadThread.start();

			m_logger.info("LogviewUploader started.");
		}
	}

	public void setAnalyzers(String analyzers) {
		m_analyzerNames = Splitters.by(',').noEmptyItem().trim().split(analyzers);
	}

	public void setExtraTime(long time) {
		m_extraTime = time;
	}

	class Period {
		private long m_startTime;

		private long m_endTime;

		private List<Task> m_tasks;

		public Period(long startTime, long endTime) {
			m_startTime = startTime;
			m_endTime = endTime;
			m_tasks = new ArrayList<Task>(m_analyzerNames.size());

			for (String name : m_analyzerNames) {
				MessageAnalyzer analyzer = m_factory.create(name, startTime, m_duration, m_extraTime);
				MessageQueue queue = lookup(MessageQueue.class);
				Task task = new Task(m_factory, analyzer, queue);

				m_tasks.add(task);
			}
		}

		public void distribute(MessageTree tree) {
			for (Task task : m_tasks) {
				task.enqueue(tree);
			}
		}

		public void finish() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate = new Date(m_startTime);
			Set<String> domains = new HashSet<String>();
			Date endDate = new Date(m_endTime - 1);

			m_logger.info(String.format("Finishing %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),
			      df.format(endDate)));

			Cat.setup(null);

			MessageProducer cat = Cat.getProducer();
			Transaction t = cat.newTransaction("Checkpoint", "RealtimeConsumer");

			try {
				for (Task task : m_tasks) {
					task.finish();
					domains.addAll(task.getAnalyzer().getDomains());
				}

				flushLogviewBuckets(domains);
				uploadLogviewBuckets(domains);

				t.setStatus(Message.SUCCESS);
			} catch (Throwable e) {
				cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}

			Cat.reset();
		}

		private void flushLogviewBuckets(Set<String> domains) {
			BucketManager manager = lookup(BucketManager.class);

			try {
				for (String domain : domains) {
					Bucket<MessageTree> bucket = manager.getLogviewBucket(m_startTime, domain);

					bucket.flush();
				}
			} catch (Exception e) {
				m_logger.warn("Error when flushing logview bucket for domains: " + domains, e);
			}
		}

		public MessageAnalyzer getAnalyzer(String name) {
			int index = m_analyzerNames.indexOf(name);

			if (index >= 0) {
				Task task = m_tasks.get(index);

				return task.getAnalyzer();
			}

			return null;
		}

		public List<MessageAnalyzer> getAnalzyers() {
			List<MessageAnalyzer> analyzers = new ArrayList<MessageAnalyzer>(m_tasks.size());

			for (Task task : m_tasks) {
				analyzers.add(task.getAnalyzer());
			}

			return analyzers;
		}

		public boolean isIn(long timestamp) {
			return timestamp >= m_startTime && timestamp < m_endTime;
		}

		public void start() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			m_logger.info(String.format("Starting %s tasks in period [%s, %s]", m_tasks.size(),
			      df.format(new Date(m_startTime)), df.format(new Date(m_endTime - 1))));

			for (Task task : m_tasks) {
				m_executor.submit(task);
			}
		}

		private void uploadLogviewBuckets(Set<String> domains) {
			for (String domain : domains) {
				m_uploader.addBucket(m_startTime, domain);
			}
		}
	}

	class PeriodManager extends Thread {
		private long m_duration;

		private List<Period> m_periods = new ArrayList<RealtimeConsumer.Period>();

		public PeriodManager(long duration) {
			m_duration = duration;
		}

		private void endPeriod(long startTime) {
			int len = m_periods.size();

			for (int i = 0; i < len; i++) {
				Period period = m_periods.get(i);

				if (period.isIn(startTime)) {
					m_periods.remove(i);
					period.finish();
					break;
				}
			}
		}

		public Period findPeriod(long timestamp) {
			for (Period period : m_periods) {
				if (period.isIn(timestamp)) {
					return period;
				}
			}

			return null;
		}

		public long getCurrentStartTime() {
			long now = System.currentTimeMillis();
			long time = now - now % m_duration;

			return time;
		}

		@Override
		public void run() {
			long now = System.currentTimeMillis();
			long startTime = now - now % m_duration;
			long lastStartTime = startTime;

			startPeriod(startTime);

			try {
				while (true) {
					now = System.currentTimeMillis();

					// prepare next period in ahead of 3 minutes
					if (now - startTime >= m_duration - 3 * MINUTE) {
						startTime = now - now % m_duration + m_duration;
						startPeriod(startTime);
					}

					// last period is over
					if (now - lastStartTime >= m_duration + m_extraTime) {
						endPeriod(lastStartTime);
						lastStartTime = startTime;
					}

					sleep(1000L);
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		private void startPeriod(long startTime) {
			long endTime = startTime + m_duration;
			Period period = new Period(startTime, endTime);

			m_periods.add(period);
			period.start();
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

		public boolean enqueue(MessageTree tree) {
			return m_queue.offer(tree);
		}

		public void finish() {
			try {
				m_analyzer.doCheckpoint(true);
			} finally {
				m_factory.release(m_analyzer);
				m_factory.release(m_queue);
			}
		}

		public MessageAnalyzer getAnalyzer() {
			return m_analyzer;
		}

		@Override
		public void run() {
			try {
				m_analyzer.analyze(m_queue);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}
}