package com.dianping.cat.consumer.problem.handler;

import java.util.HashMap;
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

public class LongUrlHandler extends Handler implements Initializable {
	@Inject
	private ServerConfigManager m_configManager;

	// private int m_defaultUrlThreshold;

	private Map<String, Integer> m_thresholds = new HashMap<String, Integer>();

	private int[] m_defaultLongUrlDuration = { 1000, 2000, 3000, 4000, 5000 };

	@Override
	public int handle(Machine machine, MessageTree tree) {
		Message message = tree.getMessage();
		int count = 0;

		if (message instanceof Transaction && "URL".equals(message.getType())) {

			long duration = ((Transaction) message).getDurationInMillis();
			String domain = tree.getDomain();

			long nomarizeDuration = getDuration(duration, domain);
			if (nomarizeDuration > 0) {
				String type = ProblemType.LONG_URL.getName();
				String status = message.getName();

				Entry entry = findOrCreatEntry(machine, type, status);
				updateEntry(tree, entry, (int) nomarizeDuration);
				count++;
			}
		}

		return count;
	}

	@Override
	public void initialize() throws InitializationException {
		Map<String, Domain> domains = m_configManager.getLongConfigDomains();

		// m_defaultUrlThreshold = m_configManager.getLongUrlDefaultThreshold();
		for (Domain domain : domains.values()) {
			if (domain.getUrlThreshold() != null) {
				m_thresholds.put(domain.getName(), domain.getUrlThreshold());
			}
		}
	}

	public int getDuration(long duration, String domain) {
		for (int i = m_defaultLongUrlDuration.length - 1; i >= 0; i--) {
			if (duration >= m_defaultLongUrlDuration[i]) {
				return m_defaultLongUrlDuration[i];
			}
		}
		Integer value = m_thresholds.get(domain);
		if (value == null) {
			return -1;
		}
		if (duration >= value) {
			return value;
		}
		return -1;
	}
}