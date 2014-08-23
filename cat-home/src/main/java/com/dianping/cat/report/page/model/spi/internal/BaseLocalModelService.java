package com.dianping.cat.report.page.model.spi.internal;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public abstract class BaseLocalModelService<T> extends ModelServiceWithCalSupport implements ModelService<T>,
      Initializable {
	@Inject(type = MessageConsumer.class)
	private RealtimeConsumer m_consumer;

	private String m_defaultDomain = "cat";

	private String m_name;

	public BaseLocalModelService(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}

	@SuppressWarnings("unchecked")
	protected T getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
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

	@Override
	public void initialize() throws InitializationException {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		m_defaultDomain = manager.getConsoleDefaultDomain();
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());

		try {
			ModelPeriod period = request.getPeriod();
			String domain = request.getDomain();
			T report = getReport(request, period, domain);

			t.addData("period", period);
			t.addData("domain", domain);

			if (report != null) {
				response.setModel(report);
				t.setStatus(Message.SUCCESS);
			} else {
				t.setStatus("NoReportFound");
			}
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
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
