package com.dianping.cat.report.service;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.RealtimeConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.mvc.ApiPayload;

public abstract class LocalModelService<T> implements Initializable {
	@Inject(type = MessageConsumer.class)
	private RealtimeConsumer m_consumer;

	@Inject
	protected ServerConfigManager m_manager;

	public static final int DEFAULT_SIZE = 32 * 1024;
	
	public static final int ANALYZER_COUNT = 2;

	private String m_defaultDomain = Constants.CAT;

	private String m_name;

	public LocalModelService(String name) {
		m_name = name;
	}

	public abstract String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception;

	public String getName() {
		return m_name;
	}

	@SuppressWarnings("unchecked")
	protected List<T> getReport(ModelPeriod period, String domain) throws Exception {
		List<MessageAnalyzer> analyzers = null;

		if (domain == null || domain.length() == 0) {
			domain = m_defaultDomain;
		}

		if (period.isCurrent()) {
			analyzers = m_consumer.getCurrentAnalyzer(m_name);
		} else if (period.isLast()) {
			analyzers = m_consumer.getLastAnalyzer(m_name);
		}

		if (analyzers == null) {
			return null;
		} else {
			List<T> list = new ArrayList<T>();

			for (MessageAnalyzer a : analyzers) {
				list.add(((AbstractMessageAnalyzer<T>) a).getReport(domain));
			}
			return list;
		}
	}

	public String getReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		try {
			return buildReport(request, period, domain, payload);
		} catch (ConcurrentModificationException e) {
			return buildReport(request, period, domain, payload);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultDomain = m_manager.getConsoleDefaultDomain();
	}

	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name);
		sb.append(']');

		return sb.toString();
	}
}
