package com.dianping.cat.report.page.top;

import java.util.ArrayList;
import java.util.Collection;

import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {
	
	private TopReport m_topReport;
	
	private TopMetric m_topMetric;
	
	private boolean m_refresh = false;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return "Cat";
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
	
	public TopReport getTopReport() {
		return m_topReport;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public TopMetric getTopMetric() {
   	return m_topMetric;
   }

	public void setTopMetric(TopMetric topMetric) {
   	m_topMetric = topMetric;
   }

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public void setTopReport(TopReport topReport) {
		m_topReport = topReport;
	}
}
