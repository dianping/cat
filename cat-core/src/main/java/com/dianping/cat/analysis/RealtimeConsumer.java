package com.dianping.cat.analysis;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.List;

@Named(type = MessageConsumer.class)
public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private PeriodManager m_periodManager;

	private Logger m_logger;

	public static final long MINUTE = 60 * 1000L;

	public static final long HOUR = 60 * MINUTE;

	@Override
	public void consume(MessageTree tree) {
		long timestamp = tree.getMessage().getTimestamp();
		Period period = m_periodManager.findPeriod(timestamp);

		if (period != null) {
			period.distribute(tree);
		} else {
			m_serverStateManager.addNetworkTimeError(1);
		}
	}

	public void doCheckpoint() {
		m_logger.info("starting do checkpoint.");
		MessageProducer cat = Cat.getProducer();
		Transaction t = cat.newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			long currentStartTime = getCurrentStartTime();
			Period period = m_periodManager.findPeriod(currentStartTime);

			for (MessageAnalyzer analyzer : period.getAnalyzers()) {
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
		m_logger.info("end do checkpoint.");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public List<MessageAnalyzer> getCurrentAnalyzer(String name) {
		long currentStartTime = getCurrentStartTime();
		Period period = m_periodManager.findPeriod(currentStartTime);

		if (period != null) {
			return period.getAnalyzer(name);
		} else {
			return null;
		}
	}

	private long getCurrentStartTime() {
		long now = System.currentTimeMillis();

		return now - now % HOUR;
	}

	@Override
	public List<MessageAnalyzer> getLastAnalyzer(String name) {
		long lastStartTime = getCurrentStartTime() - HOUR;
		Period period = m_periodManager.findPeriod(lastStartTime);

		return period == null ? null : period.getAnalyzer(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_periodManager = new PeriodManager(HOUR, m_analyzerManager, m_serverStateManager, m_logger);
		m_periodManager.init();

		Threads.forGroup("cat").start(m_periodManager);
	}

}