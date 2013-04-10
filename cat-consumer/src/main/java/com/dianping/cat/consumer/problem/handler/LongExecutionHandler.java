package com.dianping.cat.consumer.problem.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.configuration.server.entity.Domain;
import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import org.unidal.lookup.annotation.Inject;

public class LongExecutionHandler extends Handler implements Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	private int m_defaultCacheThreshold = 10;

	private int[] m_defaultLongServiceDuration = { 50, 100, 500, 1000, 3000, 5000 };

	private int[] m_defaultLongSqlDuration = { 100, 500, 1000, 3000 };

	private int[] m_defaultLongUrlDuration = { 1000, 2000, 3000, 5000 };

	private Map<String, Integer> m_longServiceThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longSqlThresholds = new HashMap<String, Integer>();

	private Map<String, Integer> m_longUrlThresholds = new HashMap<String, Integer>();

	public int computeLongDuration(long duration, String domain, int[] defaultLongDuration,
	      Map<String, Integer> longThresholds) {
		int[] messageDuration = defaultLongDuration;

		for (int i = messageDuration.length - 1; i >= 0; i--) {
			if (duration >= messageDuration[i]) {
				return messageDuration[i];
			}
		}

		Integer value = longThresholds.get(domain);

		if (value != null && duration >= value) {
			return value;
		} else {
			return -1;
		}
	}

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		count += processLongUrl(machine, tree);
		count += processLongService(machine, tree);

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count = processTransaction(machine, transaction, tree);
		}

		return count;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		for (Domain domain : domains.values()) {
			Integer serviceThreshold = domain.getServiceThreshold();
			Integer urlThreshold = domain.getUrlThreshold();
			Integer sqlThreshold = domain.getSqlThreshold();

			if (serviceThreshold != null) {
				m_longServiceThresholds.put(domain.getName(), serviceThreshold);
			}
			if (urlThreshold != null) {
				m_longUrlThresholds.put(domain.getName(), urlThreshold);
			}
			if (sqlThreshold != null) {
				m_longSqlThresholds.put(domain.getName(), sqlThreshold);
			}
		}
	}

	private int processLongCache(Machine machine, Transaction transaction, MessageTree tree, int count) {
		long duration = ((Transaction) transaction).getDurationInMillis();

		if (duration > m_defaultCacheThreshold) {
			String type = ProblemType.LONG_CACHE.getName();
			String status = transaction.getName();

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, 0);
			count++;
		}
		return count;
	}

	private int processLongService(Machine machine, MessageTree tree) {
		int count = 0;
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String messageType = message.getType();

			if ("Service".equals(messageType) || "PigeonService".equals(messageType)) {
				long duration = ((Transaction) message).getDurationInMillis();
				String domain = tree.getDomain();
				long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongServiceDuration,
				      m_longServiceThresholds);

				if (nomarizeDuration > 0) {
					String type = ProblemType.LONG_SERVICE.getName();
					String status = message.getName();

					Entry entry = findOrCreateEntry(machine, type, status);
					updateEntry(tree, entry, (int) nomarizeDuration);
					count++;
				}
			}
		}

		return count;
	}

	private int processLongSql(Machine machine, Transaction transaction, MessageTree tree, int count) {
		long duration = transaction.getDurationInMillis();
		String domain = tree.getDomain();

		long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongSqlDuration, m_longSqlThresholds);
		if (nomarizeDuration > 0) {
			String type = ProblemType.LONG_SQL.getName();
			String status = transaction.getName();

			Entry entry = findOrCreateEntry(machine, type, status);
			updateEntry(tree, entry, (int) nomarizeDuration);
			count++;
		}
		return count;
	}

	private int processLongUrl(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction && "URL".equals(message.getType())) {

			long duration = ((Transaction) message).getDurationInMillis();
			String domain = tree.getDomain();

			long nomarizeDuration = computeLongDuration(duration, domain, m_defaultLongUrlDuration, m_longUrlThresholds);
			if (nomarizeDuration > 0) {
				String type = ProblemType.LONG_URL.getName();
				String status = message.getName();

				Entry entry = findOrCreateEntry(machine, type, status);
				updateEntry(tree, entry, (int) nomarizeDuration);
				count++;
			}
		}

		return count;
	}

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		int count = 0;
		String transactionType = transaction.getType();

		if (transactionType.startsWith("Cache.")) {
			count = processLongCache(machine, transaction, tree, count);
		} else if (transactionType.equals("SQL")) {
			count = processLongSql(machine, transaction, tree, count);
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				count += processTransaction(machine, (Transaction) message, tree);
			}
		}
		return count;
	}

}
