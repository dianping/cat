package com.dianping.cat.report.page.ip;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private IpReport m_report;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public IpReport getReport() {
		return m_report;
	}

	public String getReportInJson() {
		return String.format("%2.1s", m_report);
	}

	public void setReport(IpReport report) {
		m_report = report;
	}
}
