package com.dianping.cat.report.page.highload;

import java.util.Date;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.report.ReportPage;

public class Model extends ViewModel<ReportPage, Action, Context> {

	HighloadReport m_report = new HighloadReport();

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

	public HighloadReport getReport() {
		return m_report;
	}

	public void setReport(HighloadReport report) {
		m_report = report;
	}

}
