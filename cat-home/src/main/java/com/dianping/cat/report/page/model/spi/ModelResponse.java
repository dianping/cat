package com.dianping.cat.report.page.model.spi;

public class ModelResponse<M> {
	private Exception m_exception;

	private M m_model;

	public Exception getException() {
		return m_exception;
	}

	public M getModel() {
		return m_model;
	}

	public void setException(Exception exception) {
		m_exception = exception;
	}

	public void setModel(M model) {
		m_model = model;
	}
}
