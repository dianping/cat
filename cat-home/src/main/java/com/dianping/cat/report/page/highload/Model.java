package com.dianping.cat.report.page.highload;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.highload.TransactionHighLoadUpdater.HighLoadReport;

public class Model extends ViewModel<ReportPage, Action, Context> {

	Map<String, List<HighLoadReport>> m_reports = new HashMap<String, List<HighLoadReport>>();

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

	public Map<String, List<HighLoadReport>> getReports() {
		return m_reports;
	}

	public void setReports(Map<String, List<HighLoadReport>> reports) {
		m_reports = reports;
	}

}
