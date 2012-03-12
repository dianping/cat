package com.dianping.cat.report.page.sql;

import java.util.Collection;
import java.util.Collections;

import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context>  {
	private SqlReport m_report;
		
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public SqlReport getReport() {
   	return m_report;
   }

	public void setReport(SqlReport report) {
   	this.m_report = report;
   }

	@Override
   public String getDomain() {
		if (m_report == null) {
			return null;
		} else {
			return m_report.getDomain();
		}
   }

	@Override
   public Collection<String> getDomains() {
		if (m_report == null) {
			return Collections.emptySet();
		} else {
			return m_report.getDomains();
		}
   }
}
