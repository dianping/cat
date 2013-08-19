package com.dianping.cat.report.page.bug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.transform.DefaultJsonBuilder;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.bug.Handler.ErrorStatis;

public class Model extends AbstractReportModel<Action, Context> {

	private BugReport m_bugReport;
	
	private Map<String, ErrorStatis> m_errorStatis;

	public BugReport getBugReport() {
   	return m_bugReport;
   }

	public void setBugReport(BugReport bugReport) {
   	m_bugReport = bugReport;
   }

	public Map<String, ErrorStatis> getErrorStatis() {
		return m_errorStatis;
	}

	public void setErrorStatis(Map<String, ErrorStatis> errorStatis) {
		m_errorStatis = errorStatis;
	}

	public Model(Context ctx) {
		super(ctx);
	}
	
	public String getBugs() {
		return new DefaultJsonBuilder().buildJson(m_bugReport);
   }

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	@Override
	public String getDomain() {
		return CatString.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
	
}
