package com.dianping.cat.consumer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.statistic.ServerStatisticManager;

public class RealtimeConsumer extends ContainerHolder implements MessageConsumer, Initializable, LogEnabled {

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	private Map<String, Integer> m_errorTimeDomains = new HashMap<String, Integer>();

	private PeriodManager m_periodManager;

	private long m_networkError;

	private Logger m_logger;

	public static final long MINUTE = 60 * 1000L;

	public static final long DURATION = 60 * MINUTE;

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
			logErrorInfo(tree);
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

		if (period != null) {
			return period.getAnalyzer(name);
		} else {
			return null;
		}
	}

	private long getCurrentStartTime() {
		long now = System.currentTimeMillis();
		long time = now - now % DURATION;

		return time;
	}

	public MessageAnalyzer getLastAnalyzer(String name) {
		long lastStartTime = getCurrentStartTime() - DURATION;
		Period period = m_periodManager.findPeriod(lastStartTime);

		return period == null ? null : period.getAnalyzer(name);
	}

	@Override
	public void initialize() throws InitializationException {
		m_periodManager = new PeriodManager(DURATION, m_analyzerManager, m_serverStateManager, m_logger);

		Threads.forGroup("Cat").start(m_periodManager);
	}

	private void logErrorInfo(MessageTree tree) {
		String domain = tree.getDomain();
		Integer size = m_errorTimeDomains.get(domain);

		if (size == null) {
			size = 1;
		} else {
			size++;
		}

		m_serverStateManager.addNetworkTimeError(1);
		m_errorTimeDomains.put(domain, size);
		m_networkError++;

		if (m_networkError % (CatConstants.ERROR_COUNT * 10) == 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

			m_logger.error("Error network time:" + m_errorTimeDomains);
			m_logger.error("The timestamp of message is out of range, IGNORED! "
			      + sdf.format(new Date(tree.getMessage().getTimestamp())) + " " + tree.getDomain() + " "
			      + tree.getIpAddress());
		}
	}

}