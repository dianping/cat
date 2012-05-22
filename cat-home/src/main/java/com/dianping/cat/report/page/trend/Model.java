package com.dianping.cat.report.page.trend;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private String m_graph;
	
	public String getGraph() {
   	return m_graph;
   }

	public void setGraph(String graph) {
   	m_graph = graph;
   }

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
