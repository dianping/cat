package com.dianping.cat.report.service;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class BaseHistoricalModelService<T> extends ModelServiceWithCalSupport implements ModelService<T>,
      Initializable {

	@Inject
	private ServerConfigManager m_manager;

	private boolean m_localMode = true;

	private String m_name;

	public BaseHistoricalModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_manager.isLocalMode();
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());
		t.addData("thread", Thread.currentThread());

		try {
			T model = buildModel(request);

			response.setModel(model);
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
		return request.getPeriod().isHistorical();
	}

	protected boolean isLocalMode() {
		return m_localMode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name).append(']');

		return sb.toString();
	}
}
