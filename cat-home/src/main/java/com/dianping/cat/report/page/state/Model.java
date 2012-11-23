package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	public StateReport m_report;
	
	public StateShow m_state;
	
	public Set<String> getIps(){
		if(m_report==null){
			return new HashSet<String>();
		}
		return m_report.getMachines().keySet();
	}
	
	public StateShow getState() {
		return m_state;
	}

	public void setState(StateShow state) {
		m_state = state;
	}

	public StateReport getReport() {
		return m_report;
	}

	public void setReport(StateReport reports) {
		m_report = reports;
	}

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
}
