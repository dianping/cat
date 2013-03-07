package com.dianping.cat.report.page.top;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	
	private TopReport m_topReport;
	
	private Metrix m_metrix;
	
	public Metrix getMetrix() {
		return m_metrix;
	}

	public void setMetrix(Metrix metrix) {
		m_metrix = metrix;
	}

	public TopReport getTopReport() {
		return m_topReport;
	}

	public void setTopReport(TopReport topReport) {
		m_topReport = topReport;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return "Cat";
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
}
