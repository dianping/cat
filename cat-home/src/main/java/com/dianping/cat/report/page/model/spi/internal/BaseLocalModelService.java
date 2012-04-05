package com.dianping.cat.report.page.model.spi.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class BaseLocalModelService<T> extends ModelServiceWithCalSupport implements ModelService<T> {
	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Inject
	private String m_defaultDomain;

	private String m_name;

	public BaseLocalModelService(String name) {
		m_name = name;
	}

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
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = Cat.getProducer().newTransaction("ModelService", getClass().getSimpleName());

		try {
			T report = getReport(request, request.getPeriod(), request.getDomain());

			response.setModel(report);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			logError(e);
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

	public void setDefaultDomain(String defaultDomain) {
		m_defaultDomain = defaultDomain;
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
