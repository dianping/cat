package com.dianping.cat.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.analysis.PeriodStrategy;
import com.dianping.cat.statistic.ServerStatisticManager;

public class PeriodManager implements Task {
	private PeriodStrategy m_strategy;

	private List<Period> m_periods = new ArrayList<Period>();

	private boolean m_active;

	private CountDownLatch m_latch;

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;
	
	@Inject
	private Logger m_logger;
	
	public static long EXTRATIME = 3 * 60 * 1000L;

	public PeriodManager(long duration,  MessageAnalyzerManager analyzerManager,
	      ServerStatisticManager serverStateManager, Logger logger) {
		m_strategy = new PeriodStrategy(duration, EXTRATIME, EXTRATIME);
		m_active = true;
		m_latch = new CountDownLatch(1);
		m_analyzerManager = analyzerManager;
		m_serverStateManager = serverStateManager;
		m_logger = logger;
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
		Period period = new Period(startTime, endTime, m_analyzerManager, m_serverStateManager, m_logger);

		m_periods.add(period);
		period.start();
	}

	public void waitUntilStarted() throws InterruptedException {
		m_latch.await();
	}

	private class EndTaskThread implements Task {

		private long m_startTime;

		public EndTaskThread(long startTime) {
			m_startTime = startTime;
		}

		@Override
		public String getName() {
			return "End-Consumer-Task";
		}

		@Override
		public void run() {
			endPeriod(m_startTime);
		}

		@Override
		public void shutdown() {
		}
	}
}