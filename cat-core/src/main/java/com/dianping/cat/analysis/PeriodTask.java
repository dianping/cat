package com.dianping.cat.analysis;

import java.util.Calendar;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PeriodTask implements Task, LogEnabled {

	private MessageAnalyzer m_analyzer;

	private MessageQueue m_queue;

	private long m_startTime;

	private int m_queueOverflow;

	private Logger m_logger;

	private int m_index;

	public void setIndex(int index) {
		m_index = index;
	}

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

			if (m_queueOverflow % (10 * CatConstants.ERROR_COUNT) == 0) {
				m_logger.warn(m_analyzer.getClass().getSimpleName() + " queue overflow number " + m_queueOverflow);
			}

			// fix issue #1155
			if (m_analyzer.getClass().getSimpleName().equals("DumpAnalyzer") && tree instanceof DefaultMessageTree) {
				DefaultMessageTree t = (DefaultMessageTree) tree;

				if (t.getBuffer() != null) {
					t.getBuffer().release();
				}
			}
		}

		return result;
	}

	public void finish() {
		try {
			m_analyzer.doCheckpoint(true);
			m_analyzer.destroy();
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public MessageAnalyzer getAnalyzer() {
		return m_analyzer;
	}

	@Override
	public String getName() {
		Calendar cal = Calendar.getInstance();

		cal.setTimeInMillis(m_startTime);
		return m_analyzer.getClass().getSimpleName() + "-" + cal.get(Calendar.HOUR_OF_DAY) + "-" + m_index;
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