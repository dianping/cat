package com.dianping.cat.report.service;

import java.util.ConcurrentModificationException;

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
	protected T getReport(ModelPeriod period, String domain) throws Exception {
		MessageAnalyzer analyzer = null;

		if (period.isCurrent() || period.isFuture()) {
			analyzer = m_consumer.getCurrentAnalyzer(m_name);
		} else if (period.isLast()) {
			analyzer = m_consumer.getLastAnalyzer(m_name);
		}

		if (analyzer == null) {
			return null;
		} else if (analyzer instanceof AbstractMessageAnalyzer) {
			AbstractMessageAnalyzer<T> a = (AbstractMessageAnalyzer<T>) analyzer;

			if (domain == null || domain.length() == 0) {
				return a.getReport(m_defaultDomain);
			} else {
				return a.getReport(domain);
			}
		}

		throw new RuntimeException("Internal error: this should not be reached!");
	}

	public String getReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload) throws Exception {
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
