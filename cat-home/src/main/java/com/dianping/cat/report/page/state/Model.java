package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

@ModelMeta(StateAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	public StateReport m_report;

	@EntityMeta
	public StateDisplay m_state;

	public String m_message;

	public String m_graph;

	public String m_pieChart;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
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
		return SortHelper.sortIpAddress(m_report.getMachines().keySet());
	}

	public String getMessage() {
		return m_message;
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public StateReport getReport() {
		return m_report;
	}

	public StateDisplay getState() {
		return m_state;
	}

	public void setGraph(String graph) {
		m_graph = graph;
	}

	public void setMessage(String message) {
		m_message = message;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

	public void setReport(StateReport reports) {
		m_report = reports;
	}

	public void setState(StateDisplay state) {
		m_state = state;
	}

}
