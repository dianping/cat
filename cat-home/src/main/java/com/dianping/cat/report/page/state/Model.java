package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	public StateReport m_reports;
	
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
