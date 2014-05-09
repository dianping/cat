package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta(StateAnalyzer.ID)
public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	public StateReport m_report;

	@EntityMeta
	public StateShow m_state;
	
	public String m_message;

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
	
	public String getMessage() {
   	return m_message;
   }

	public void setMessage(String message) {
   	m_message = message;
   }

	public void setReport(StateReport reports) {
		m_report = reports;
	}

	public void setState(StateShow state) {
		m_state = state;
	}
}
