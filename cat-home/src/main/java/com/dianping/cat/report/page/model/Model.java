package com.dianping.cat.report.page.model;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private Throwable m_exception;

	private Object m_model;

	private String m_modelInXml;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.XML;
	}

	public Throwable getException() {
		return m_exception;
	}

	public Object getModel() {
		return m_model;
	}

	public String getModelInXml() {
		return m_modelInXml;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	public void setModel(Object model) {
		m_model = model;
	}

	public void setModelInXml(String modelInXml) {
		m_modelInXml = modelInXml;
	}
}
