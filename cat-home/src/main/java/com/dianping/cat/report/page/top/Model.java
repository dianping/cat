package com.dianping.cat.report.page.top;

import java.util.ArrayList;
import java.util.Collection;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.page.AbstractReportModel;

@ModelMeta(TopAnalyzer.ID)
public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	private TopReport m_topReport;

	@EntityMeta
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
		return Constants.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public TopMetric getTopMetric() {
		return m_topMetric;
	}

	public TopReport getTopReport() {
		return m_topReport;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public void setTopMetric(TopMetric topMetric) {
		m_topMetric = topMetric;
	}

	public void setTopReport(TopReport topReport) {
		m_topReport = topReport;
	}
}
