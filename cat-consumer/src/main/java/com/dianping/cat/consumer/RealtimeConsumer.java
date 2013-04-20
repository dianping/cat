package com.dianping.cat.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.ServerStateManager;

/**
 * This is the real time consumer process framework.
 * <p>
 */
public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {
	public static final String ID = "realtime";

	private static final long MINUTE = 60 * 1000L;
	
	@Inject
	private ServerStateManager m_serverStateManager;

	@Inject
	private MessageAnalyzerFactory m_factory;

	@Inject
	private long m_duration = 60 * MINUTE;

	@Inject
	private long m_extraTime = 3 * MINUTE;

	@Inject
	private List<String> m_analyzerNames;

	private Set<String> m_domains = new HashSet<String>();

	private Map<String, Integer> m_errorTimeDomains = new HashMap<String, Integer>();

	private Logger m_logger;

	private PeriodManager m_periodManager;

	private CountDownLatch m_latch;

	private long m_networkError;

	@Override
	public void consume(MessageTree tree) {
		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}

		long timestamp = tree.getMessage().getTimestamp();
		Period period = m_periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);

			String domain = tree.getDomain();

			if (!m_domains.contains(domain)) {
				m_domains.add(domain);
			}
		} else {
			m_serverStateManager.addNetworkTimeError(1);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			String domain = tree.getDomain();
			Integer size = m_errorTimeDomains.get(domain);

			if (size == null) {
				size = 1;
			} else {
				size++;
			}
			m_errorTimeDomains.put(domain, size);

			m_networkError++;
			if (m_networkError % (CatConstants.ERROR_COUNT * 10) == 0) {
				m_logger.error("Error network time:" + m_errorTimeDomains);
				m_logger.error("The timestamp of message is out of range, IGNORED! "
				      + sdf.format(new Date(tree.getMessage().getTimestamp())) + " " + tree.getDomain() + " " + tree.getIpAddress());
			}
		}
	}

	public void doCheckpoint() throws IOException {
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			long currentStartTime = getCurrentStartTime();
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

	public MessageAnalyzer getCurrentAnalyzer(String name) {
		long currentStartTime = getCurrentStartTime();
		Period period = m_periodManager.findPeriod(currentStartTime);

		return period.getAnalyzer(name);
	}

	private long getCurrentStartTime() {
		long now = System.currentTimeMillis();
		long time = now - now % m_duration;

		return time;
	}

	public MessageAnalyzer getLastAnalyzer(String name) {
		long lastStartTime = getCurrentStartTime() - m_duration;
		Period period = m_periodManager.findPeriod(lastStartTime);

		return period == null ? null : period.getAnalyzer(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_latch = new CountDownLatch(1);
		m_periodManager = new PeriodManager(m_latch);

		Threads.forGroup("Cat").start(m_periodManager);
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

		private List<PeriodTask> m_tasks;

		public Period(long startTime, long endTime) {
			m_startTime = startTime;
			m_endTime = endTime;
			m_tasks = new ArrayList<PeriodTask>(m_analyzerNames.size());

			Map<String, MessageAnalyzer> analyzers = new HashMap<String, MessageAnalyzer>();

			for (String name : m_analyzerNames) {
				MessageAnalyzer analyzer = m_factory.create(name, startTime, m_duration, m_extraTime);
				MessageQueue queue = lookup(MessageQueue.class);
				PeriodTask task = new PeriodTask(analyzer, queue, startTime);

				analyzers.put(name, analyzer);
				task.enableLogging(m_logger);
				m_tasks.add(task);
			}

			// hack for dependency
			MessageAnalyzer top = analyzers.get(TopAnalyzer.ID);
			MessageAnalyzer transaction = analyzers.get(TransactionAnalyzer.ID);
			MessageAnalyzer problem = analyzers.get(ProblemAnalyzer.ID);

			((TopAnalyzer) top).setTransactionAnalyzer((TransactionAnalyzer) transaction);
			((TopAnalyzer) top).setProblemAnalyzer((ProblemAnalyzer) problem);
		}

		public void distribute(MessageTree tree) {
			for (PeriodTask task : m_tasks) {
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
				for (PeriodTask task : m_tasks) {
					task.finish();
					domains.addAll(task.getAnalyzer().getDomains());
				}

				t.setStatus(Message.SUCCESS);
			} catch (Throwable e) {
				cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();

				m_logger.info(String.format("Finished %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),
				      df.format(endDate)));
			}

			Cat.reset();
		}

		public MessageAnalyzer getAnalyzer(String name) {
			int index = m_analyzerNames.indexOf(name);

			if (index >= 0) {
				PeriodTask task = m_tasks.get(index);

				return task.getAnalyzer();
			}

			return null;
		}

		public List<MessageAnalyzer> getAnalzyers() {
			List<MessageAnalyzer> analyzers = new ArrayList<MessageAnalyzer>(m_tasks.size());

			for (PeriodTask task : m_tasks) {
				analyzers.add(task.getAnalyzer());
			}

			return analyzers;
		}

		public boolean isIn(long timestamp) {
			return timestamp >= m_startTime && timestamp < m_endTime;
		}

		public void start() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			m_logger.info(String.format("Starting %s tasks in period [%s, %s]", m_tasks.size(), df.format(new Date(m_startTime)),
			      df.format(new Date(m_endTime - 1))));

			for (PeriodTask task : m_tasks) {
				Threads.forGroup("Cat-RealtimeConsumer").start(task);
			}
		}
	}

	class PeriodManager implements Task {
		private PeriodStrategy m_strategy;

		private List<Period> m_periods = new ArrayList<RealtimeConsumer.Period>();

		private boolean m_active;

		private CountDownLatch m_latch;

		public PeriodManager(CountDownLatch latch) {
			m_strategy = new PeriodStrategy(m_duration, m_extraTime, 3 * MINUTE);
			m_active = true;
			m_latch = latch;
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

		@Override
		public String getName() {
			return "RealtimeConsumer-PeriodManager";
		}

		@Override
		public void run() {
			long startTime = m_strategy.next(System.currentTimeMillis());

			// for current period
			startPeriod(startTime);
			m_latch.countDown();

			try {
				while (m_active) {
					long now = System.currentTimeMillis();
					long value = m_strategy.next(now);

					if (value == 0) {
						// do nothing here
					} else if (value > 0) {
						// prepare next period in ahead of 3 minutes
						startPeriod(value);
					} else {
						// last period is over
						endPeriod(-value);
					}

					Thread.sleep(1000L);
				}
			} catch (InterruptedException e) {
				// ignore it
			}
		}

		@Override
		public void shutdown() {
			m_active = false;
		}

		private void startPeriod(long startTime) {
			long endTime = startTime + m_duration;
			Period period = new Period(startTime, endTime);

			m_periods.add(period);
			period.start();
		}
	}

	public class PeriodStrategy {
		private long m_duration;

		private long m_extraTime;

		private long m_aheadTime;

		private long m_lastStartTime;

		private long m_lastEndTime;

		public PeriodStrategy(long duration, long extraTime, long aheadTime) {
			m_duration = duration;
			m_extraTime = extraTime;
			m_aheadTime = aheadTime;
			m_lastStartTime = -1;
			m_lastEndTime = 0;
		}

		public long next(long now) {
			long startTime = now - now % m_duration;

			// for current period
			if (startTime > m_lastStartTime) {
				m_lastStartTime = startTime;
				return startTime;
			}

			// prepare next period ahead
			if (now - m_lastStartTime >= m_duration - m_aheadTime) {
				m_lastStartTime = startTime + m_duration;
				return startTime + m_duration;
			}

			// last period is over
			if (now - m_lastEndTime >= m_duration + m_extraTime) {
				long lastEndTime = m_lastEndTime;
				m_lastEndTime = startTime;
				return -lastEndTime;
			}

			return 0;
		}
	}

	public class PeriodTask implements Task, LogEnabled {
		private MessageAnalyzer m_analyzer;

		private MessageQueue m_queue;

		private long m_startTime;

		private int m_queueOverflow;

		private Logger m_logger;

		public PeriodTask(MessageAnalyzer analyzer, MessageQueue queue, long startTime) {
			m_analyzer = analyzer;
			m_queue = queue;
			m_startTime = startTime;
		}

		@Override
		public void enableLogging(Logger logger) {
			m_logger = logger;
		}

		public boolean enqueue(MessageTree tree) {
			boolean result = m_queue.offer(tree);

			if (!result) { // trace queue overflow
				m_queueOverflow++;
				if (m_queueOverflow % CatConstants.ERROR_COUNT == 0) {
					m_serverStateManager.addMessageTotalLoss(CatConstants.ERROR_COUNT);
					m_logger.warn(m_analyzer.getClass().getSimpleName() + " queue overflow number " + m_queueOverflow);
				}
			}
			return result;
		}

		public void finish() {
			try {
				m_analyzer.doCheckpoint(true);
			} finally {
				m_analyzer.destroy();
				m_queue.destroy();
			}
		}

		public MessageAnalyzer getAnalyzer() {
			return m_analyzer;
		}

		@Override
		public String getName() {
			Calendar cal = Calendar.getInstance();

			cal.setTimeInMillis(m_startTime);
			return m_analyzer.getClass().getSimpleName() + "-" + cal.get(Calendar.HOUR_OF_DAY);
		}

		@Override
		public void run() {
			try {
				m_analyzer.analyze(m_queue);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		@Override
		public void shutdown() {
			if (m_analyzer instanceof AbstractMessageAnalyzer) {
				((AbstractMessageAnalyzer<?>) m_analyzer).shutdown();
			}
		}
	}
}