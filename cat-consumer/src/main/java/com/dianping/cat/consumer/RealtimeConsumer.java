package com.dianping.cat.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.analysis.PeriodStrategy;
import com.dianping.cat.analysis.PeriodTask;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.statistic.ServerStatisticManager;

public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {
	private static final long MINUTE = 60 * 1000L;

	private static int QUEUE_SIZE = 200000;

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private Map<String, Integer> m_errorTimeDomains = new HashMap<String, Integer>();

	private Logger m_logger;

	private PeriodManager m_periodManager;

	private long m_networkError;

	private long m_duration = 60 * MINUTE;

	private long m_extraTime = 3 * MINUTE;

	@Override
	public void consume(MessageTree tree) {
		try {
			m_periodManager.waitUntilStarted();
		} catch (InterruptedException e) {
			// ignore it
		}

		long timestamp = tree.getMessage().getTimestamp();
		Period period = m_periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);
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
				      + sdf.format(new Date(tree.getMessage().getTimestamp())) + " " + tree.getDomain() + " "
				      + tree.getIpAddress());
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
				try {
					analyzer.doCheckpoint(false);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			try {
				// wait dump analyzer store completed
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				// ignore
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
		m_periodManager = new PeriodManager();

		Threads.forGroup("Cat").start(m_periodManager);
	}

	class Period {
		private long m_startTime;

		private long m_endTime;

		private List<PeriodTask> m_tasks;

		public Period(long startTime, long endTime) {
			List<String> names = m_analyzerManager.getAnalyzerNames();

			m_startTime = startTime;
			m_endTime = endTime;
			m_tasks = new ArrayList<PeriodTask>(names.size());

			Map<String, MessageAnalyzer> analyzers = new LinkedHashMap<String, MessageAnalyzer>();

			for (String name : names) {
				MessageAnalyzer analyzer = m_analyzerManager.getAnalyzer(name, startTime);
				MessageQueue queue = new DefaultMessageQueue(QUEUE_SIZE);
				PeriodTask task = new PeriodTask(m_serverStateManager, analyzer, queue, startTime);

				analyzers.put(name, analyzer);
				task.enableLogging(m_logger);
				m_tasks.add(task);
			}

			// hack for dependency
			MessageAnalyzer top = analyzers.get(TopAnalyzer.ID);
			MessageAnalyzer transaction = analyzers.get(TransactionAnalyzer.ID);
			MessageAnalyzer problem = analyzers.get(ProblemAnalyzer.ID);

			if (top != null) {
				((TopAnalyzer) top).setTransactionAnalyzer((TransactionAnalyzer) transaction);
				((TopAnalyzer) top).setProblemAnalyzer((ProblemAnalyzer) problem);
			}
		}

		public void distribute(MessageTree tree) {
			m_serverStateManager.addMessageTotal(tree.getDomain(), 1);
			
			for (PeriodTask task : m_tasks) {
				task.enqueue(tree);
			}
		}

		public void finish() {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date startDate = new Date(m_startTime);
			Date endDate = new Date(m_endTime - 1);

			m_logger.info(String.format("Finishing %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),
			      df.format(endDate)));

			Cat.setup(null);

			MessageProducer cat = Cat.getProducer();
			Transaction t = cat.newTransaction("Checkpoint", "RealtimeConsumer");

			try {
				for (PeriodTask task : m_tasks) {
					task.finish();
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
			List<String> names = m_analyzerManager.getAnalyzerNames();
			int index = names.indexOf(name);

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

			m_logger.info(String.format("Starting %s tasks in period [%s, %s]", m_tasks.size(),
			      df.format(new Date(m_startTime)), df.format(new Date(m_endTime - 1))));

			for (PeriodTask task : m_tasks) {
				Threads.forGroup("Cat-RealtimeConsumer").start(task);
			}
		}
	}

	class PeriodManager implements Task {
		private PeriodStrategy m_strategy;

		private List<Period> m_periods = new ArrayList<Period>();

		private boolean m_active;

		private CountDownLatch m_latch;

		public PeriodManager() {
			m_strategy = new PeriodStrategy(m_duration, m_extraTime, m_extraTime);
			m_active = true;
			m_latch = new CountDownLatch(1);
		}

		public void waitUntilStarted() throws InterruptedException {
			m_latch.await();
		}

		private void endPeriod(long startTime) {
			int len = m_periods.size();

			for (int i = 0; i < len; i++) {
				Period period = m_periods.get(i);

				if (period.isIn(startTime)) {
					period.finish();
					m_periods.remove(i);
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
			try {
				startPeriod(startTime);
				m_latch.countDown();

				while (m_active) {
					try {
						long now = System.currentTimeMillis();
						long value = m_strategy.next(now);

						if (value > 0) {
							startPeriod(value);
						} else if (value < 0) {
							// last period is over,make it asynchronous
							Threads.forGroup("Cat").start(new EndTaskThread(-value));
						}
					} catch (Throwable e) {
						Cat.logError(e);
					}

					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						break;
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		@Override
		public void shutdown() {
			m_active = false;
		}

		private void startPeriod(long startTime) {
			long endTime = startTime + m_strategy.getDuration();
			Period period = new Period(startTime, endTime);

			m_periods.add(period);
			period.start();
		}

		private class EndTaskThread implements Task {

			private long m_startTime;

			public EndTaskThread(long startTime) {
				m_startTime = startTime;
			}

			@Override
			public void run() {
				endPeriod(m_startTime);
			}

			@Override
			public String getName() {
				return "End-Consumer-Task";
			}

			@Override
			public void shutdown() {
			}
		}
	}
}