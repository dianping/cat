package com.dianping.cat.report.page.overload;

import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.overload.task.OverloadReport;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private List<OverloadReport> m_reports;

	public Model(Context ctx) {
		super(ctx);
	}

	public Date getDate() {
		return new Date();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return Constants.CAT;
	}

	public String getIpAddress() {
		return null;
	}

	public List<OverloadReport> getReports() {
		return m_reports;
	}

	public void setReports(List<OverloadReport> reports) {
		m_reports = reports;
	}
}
