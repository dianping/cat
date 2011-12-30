package com.dianping.cat.consumer.impl2;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class RealtimeConsumer extends ContainerHolder implements
		MessageConsumer, Initializable {
	@Inject
	private String m_consumerId;

	@Inject
	private String m_domain;

	@Inject
	private long m_duration = 3600 * 1000L; // 1 hour

	@Inject
	private List<String> m_analyzerNames;

	private List<PeriodicTask> m_tasks;

	@Override
	public void consume(MessageTree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConsumerId() {
		return m_consumerId;
	}

	@Override
	public String getDomain() {
		return m_domain;
	}

	@Override
	public void initialize() throws InitializationException {
		long startTime = 0; // TODO
		m_tasks = new ArrayList<PeriodicTask>();
		for (String name : m_analyzerNames) {
			MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, name);
			MessageQueue queue = lookup(MessageQueue.class);
			PeriodicTask task = new PeriodicTask(startTime, m_duration,
					analyzer, queue);

			m_tasks.add(task);
		}

		for (PeriodicTask task : m_tasks) {
			Thread thread =new Thread(task);
			thread.start();
		}
	}

	public void setAnalyzerNames(String analyzerNames) {
		m_analyzerNames = Splitters.by(',').noEmptyItem().trim()
				.split(analyzerNames);
	}

	public void setConsumerId(String consumerId) {
		m_consumerId = consumerId;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}
}
