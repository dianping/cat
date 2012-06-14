package com.dianping.cat.consumer.problem.handler;

import java.util.HashMap;
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

public class LongUrlHandler implements Handler, Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	private int m_defaultUrlThreshold;

	private Map<String, Integer> m_thresholds = new HashMap<String, Integer>();

	@Override
	public int handle(Segment segment, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction && "URL".equals(message.getType())) {
			long duration = ((Transaction) message).getDurationInMillis();
			Integer threshold = m_thresholds.get(tree.getDomain());
			long value = threshold != null ? threshold.longValue() : m_defaultUrlThreshold;

			if (duration > value) {
				Entry entry = new Entry();
				entry.setMessageId(tree.getMessageId());
				entry.setStatus(message.getName());
				entry.setType(ProblemType.LONG_URL.getName());
				entry.setDuration((int) duration);

				segment.addEntry(entry);
				count++;
			}
		}

		return count;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		m_defaultUrlThreshold = m_configManager.getLongUrlDefaultThreshold();

		for (Domain domain : domains.values()) {
			if (domain.getUrlThreshold() != null) {
				m_thresholds.put(domain.getName(), domain.getUrlThreshold());
			}
		}
	}
}