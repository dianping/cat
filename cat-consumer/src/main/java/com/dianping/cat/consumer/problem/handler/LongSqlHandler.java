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
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class LongSqlHandler implements Handler, Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	private int m_defaultSqlThreshold;

	private Map<String, Integer> m_thresholds = new HashMap<String, Integer>();

	private int processTransaction(Segment segment, Transaction transaction, MessageTree tree) {
		int count = 0;
		String type = transaction.getType();

		if (type.equals("SQL")) {
			long duration = transaction.getDurationInMillis();
			Integer threshold = m_thresholds.get(tree.getDomain());
			long value = threshold != null ? threshold.longValue() : m_defaultSqlThreshold;
			
			if (duration > value) {
				String messageId = tree.getMessageId();
				Entry entry = new Entry(messageId);

				entry.setStatus(transaction.getName());
				entry.setType(ProblemType.LONG_SQL.getName());
				entry.setDuration((int) duration);
				segment.addEntry(entry);
				count++;
			}
		}

		List<Message> messageList = transaction.getChildren();

		for (Message message : messageList) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(segment, temp, tree);
			}
		}
		return count;
	}

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction) {
			Transaction transaction = (Transaction) message;

			count = processTransaction(segment, transaction, tree);
		}

		return count;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		m_defaultSqlThreshold = m_configManager.getLongSqlDefaultThreshold();

		for (Domain domain : domains.values()) {
			if (domain.getSqlThreshold() != null) {
				m_thresholds.put(domain.getName(), domain.getSqlThreshold());
			}
		}
	}
}