package com.dianping.cat.report.page.activity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model  extends AbstractReportModel<Action, Context> {

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

	@Override
   public String getDomain() {
	   return Constants.CAT;
   }

	@Override
   public Collection<String> getDomains() {
	   return new HashSet<String>();
   }
}
