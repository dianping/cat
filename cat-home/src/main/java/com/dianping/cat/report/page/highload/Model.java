package com.dianping.cat.report.page.highload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Constants;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.highload.HighLoadSqlUpdater.HighLoadSQLReport;

public class Model extends ViewModel<ReportPage, Action, Context> {
	List<HighLoadSQLReport> m_sqlReports = new ArrayList<HighLoadSQLReport>();

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

	public List<HighLoadSQLReport> getSqlReports() {
		return m_sqlReports;
	}

	public void setSqlReports(List<HighLoadSQLReport> sqlReports) {
		m_sqlReports = sqlReports;
	}
}
