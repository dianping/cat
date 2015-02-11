package com.dianping.cat.report.page.activity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {

	private Date m_start;

	private Date m_end;
	
	private Map<String,List<LineChart>> m_charts;
	
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
