package com.dianping.cat.report.page.problem;

import java.util.Collection;
import java.util.Collections;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	private FailureReport m_report;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public Collection<String> getDomains() {
		return Collections.emptyList();
	}

	public FailureReport getReport() {
		return m_report;
	}

	public void setReport(FailureReport report) {
		m_report = report;
	}
}
