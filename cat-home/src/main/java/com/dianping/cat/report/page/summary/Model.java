package com.dianping.cat.report.page.summary;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.Constants;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private String m_summaryContent;

	public Model(Context ctx) {
		super(ctx);
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

	public String getSummaryContent() {
		return m_summaryContent;
	}

	public void setSummaryContent(String summaryContent) {
		m_summaryContent = summaryContent;
	}
}
