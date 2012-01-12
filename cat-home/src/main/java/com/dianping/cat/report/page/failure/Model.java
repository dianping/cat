package com.dianping.cat.report.page.failure;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private FailureReport m_report;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public FailureReport getReport() {
		return m_report;
	}

	public String getReportInJson() {
		DefaultJsonBuilder builder = new DefaultJsonBuilder();

		m_report.accept(builder);
		return builder.getString();
	}

	public void setReport(FailureReport report) {
		m_report = report;
	}
}
