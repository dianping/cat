package com.dianping.cat.report.page.model.spi.internal;

import com.dianping.cat.Cat;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public abstract class BaseHistoricalModelService<T> implements ModelService<T> {
	private String m_name;

	public BaseHistoricalModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	public String getName() {
		return m_name;
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();

		try {
			T model = buildModel(request);

			response.setModel(model);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			response.setException(e);
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
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
