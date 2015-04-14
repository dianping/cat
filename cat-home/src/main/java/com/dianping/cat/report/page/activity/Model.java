package com.dianping.cat.report.page.activity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private Date m_start;

	private Date m_end;

	private Map<String, List<LineChart>> m_charts;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, List<LineChart>> getCharts() {
		return m_charts;
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
		return new HashSet<String>();
	}

	public Date getEnd() {
		return m_end;
	}

	public Date getStart() {
		return m_start;
	}

	public void setCharts(Map<String, List<LineChart>> charts) {
		m_charts = charts;
	}

	public void setEnd(Date end) {
		m_end = end;
	}

	public void setStart(Date start) {
		m_start = start;
	}
}
