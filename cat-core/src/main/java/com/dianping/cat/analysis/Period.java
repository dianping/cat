package com.dianping.cat.analysis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public class Period {
	private long m_startTime;

	private long m_endTime;

	private List<PeriodTask> m_tasks;

	@Inject
	private MessageAnalyzerManager m_analyzerManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private Logger m_logger;

	private static int QUEUE_SIZE = 30000;

	public Period(long startTime, long endTime, MessageAnalyzerManager analyzerManager,
	      ServerStatisticManager serverStateManager, Logger logger) {
		m_startTime = startTime;
		m_endTime = endTime;
		m_analyzerManager = analyzerManager;
		m_serverStateManager = serverStateManager;
		m_logger = logger;

		List<String> names = m_analyzerManager.getAnalyzerNames();
		Map<String, MessageAnalyzer> analyzers = new LinkedHashMap<String, MessageAnalyzer>();

		m_tasks = new ArrayList<PeriodTask>(names.size());
		for (String name : names) {
			MessageAnalyzer analyzer = m_analyzerManager.getAnalyzer(name, startTime);
			MessageQueue queue = new DefaultMessageQueue(QUEUE_SIZE);
			PeriodTask task = new PeriodTask(analyzer, queue, startTime);

			analyzers.put(name, analyzer);
			task.enableLogging(m_logger);
			m_tasks.add(task);
		}
	}

	public void distribute(MessageTree tree) {
		m_serverStateManager.addMessageTotal(tree.getDomain(), 1);
		boolean success = true;

		for (PeriodTask task : m_tasks) {
			boolean enqueue = task.enqueue(tree);

			if (enqueue == false) {
				success = false;
			}
		}

		if (!success) {
			m_serverStateManager.addMessageTotalLoss(tree.getDomain(), 1);
		}
	}

	public void finish() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = new Date(m_startTime);
		Date endDate = new Date(m_endTime - 1);

		m_logger.info(String.format("Finishing %s tasks in period [%s, %s]", m_tasks.size(), df.format(startDate),
		      df.format(endDate)));

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
