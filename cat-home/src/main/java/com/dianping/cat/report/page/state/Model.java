package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	public StateReport m_report;

	public StateShow m_state;

	public String m_graph;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY;
	}

	@Override
	public String getDomain() {
		return "Cat";
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getGraph() {
		return m_graph;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		}
		return StringSortHelper.sortDomain(m_report.getMachines().keySet());
	}

	public StateReport getReport() {
		return m_report;
	}

	public StateShow getState() {
		return m_state;
	}

	public void setGraph(String graph) {
		m_graph = graph;
	}

	public void setReport(StateReport reports) {
		m_report = reports;
	}

	public void setState(StateShow state) {
		m_state = state;
	}
}
