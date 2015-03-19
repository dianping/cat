package com.dianping.cat.report.page.alteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.alteration.Handler.AlterationMinute;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private String m_insertResult;

	private Map<String, AlterationMinute> m_alterationMinuites;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, AlterationMinute> getAlterationMinuites() {
		return m_alterationMinuites;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return Constants.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getInsertResult() {
		return m_insertResult;
	}

	public void setAlterationMinuites(Map<String, AlterationMinute> alterationMinuites) {
		m_alterationMinuites = alterationMinuites;
	}

	public void setInsertResult(String insertResult) {
		m_insertResult = insertResult;
	}

}
