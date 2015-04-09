package com.dianping.cat.analysis;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.statistic.ServerStatisticManager;

public class PeriodManager implements Task {

	private List<Period> m_periods = new ArrayList<Period>();

	private boolean m_active;

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private Logger m_logger;

	public static long MINUTE = 60 * 1000L;

	public static long DURATION = 60 * MINUTE;

	public static long EXTRATIME = 2 * MINUTE;

	public PeriodManager(long duration, MessageAnalyzerManager analyzerManager,
	      ServerStatisticManager serverStateManager, Logger logger) {
		m_active = true;
		m_analyzerManager = analyzerManager;
		m_serverStateManager = serverStateManager;
		m_logger = logger;
	}

	private void endPeriod(long timestamp) {
		int len = m_periods.size();

		for (int i = 0; i < len; i++) {
			Period period = m_periods.get(i);

			if (period.getStartTime() <= timestamp) {
				Threads.forGroup("cat").start(new EndTaskThread(period));

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

	public void init() {
		long curTime = System.currentTimeMillis();
		long currentDuration = curTime - curTime % DURATION;

		startPeriod(currentDuration);
	}

	@Override
	public void run() {
		while (m_active) {
			try {
				long curTime = System.currentTimeMillis();

				try {
					long currentDuration = curTime - curTime % DURATION;
					long currentMinute = curTime - curTime % MINUTE;

					startPeriod(currentDuration);
					startPeriod(currentDuration + DURATION);
					endPeriod(currentMinute - DURATION - EXTRATIME);
				} catch (Exception e) {
					Cat.logError(e);
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
	}

	@Override
	public void shutdown() {
		m_active = false;
	}

	private void startPeriod(long startTime) {
		if (findPeriod(startTime) == null) {
			long endTime = startTime + DURATION;
			Period period = new Period(startTime, endTime, m_analyzerManager, m_serverStateManager, m_logger);

			m_periods.add(period);
			period.start();
		}
	}

	private class EndTaskThread implements Task {

		private Period m_period;

		public EndTaskThread(Period period) {
			m_period = period;
		}

		@Override
		public String getName() {
			return "End-Consumer-Task";
		}

		@Override
		public void run() {
			m_period.finish();
		}

		@Override
		public void shutdown() {
		}
	}

}