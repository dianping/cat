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
import com.site.lookup.annotation.Inject;

public class LongSqlHandler extends Handler implements Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	// private int m_defaultSqlThreshold;

	private Map<String, Integer> m_thresholds = new HashMap<String, Integer>();

	private int[] m_defaultLongSqlDuration = { 100, 500, 1000 };

	private int processTransaction(Machine machine, Transaction transaction, MessageTree tree) {
		int count = 0;
		String transactionType = transaction.getType();

		if (transactionType.equals("SQL")) {
			long duration = transaction.getDurationInMillis();
			String domain = tree.getDomain();

			long nomarizeDuration = getDuration(duration, domain);
			if (nomarizeDuration > 0) {
				String type = ProblemType.LONG_SQL.getName();
				String status = transaction.getName();

				Entry entry = findOrCreatEntry(machine, type, status);
				updateEntry(tree, entry ,(int)nomarizeDuration);
				count++;
			}
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(machine, temp, tree);
			}
		}
		return count;
	}

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count = processTransaction(machine, transaction, tree);
		}

		return count;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		// m_defaultSqlThreshold = m_configManager.getLongSqlDefaultThreshold();

		for (Domain domain : domains.values()) {
			if (domain.getSqlThreshold() != null) {
				m_thresholds.put(domain.getName(), domain.getSqlThreshold());
			}
		}
	}

	public int getDuration(long duration, String domain) {
		for (int i = m_defaultLongSqlDuration.length - 1; i >= 0; i--) {
			if (duration >= m_defaultLongSqlDuration[i]) {
				return m_defaultLongSqlDuration[i];
			}
		}
		Integer value = m_thresholds.get(domain);
		if (duration >= value) {
			return value;
		}
		return -1;
	}
}