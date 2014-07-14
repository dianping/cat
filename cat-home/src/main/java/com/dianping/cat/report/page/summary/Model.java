package com.dianping.cat.report.page.summary;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	
	private String m_summaryContent;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getSummaryContent() {
		return m_summaryContent;
	}

	public void setSummaryContent(String summaryContent) {
		m_summaryContent = summaryContent;
	}
}
