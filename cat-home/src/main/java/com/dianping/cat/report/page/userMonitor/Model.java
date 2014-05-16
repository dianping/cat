package com.dianping.cat.report.page.userMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;

public class Model extends AbstractReportModel<Action, Context> {

	private List<PatternItem> m_pattermItems;

	private List<String> m_cities;

	private Map<String, LineChart> m_lineCharts;
	
	private LineChart m_lineChart;
	
	private PieChart m_pieChart;
	
	private Date m_start;

	private Date m_end;

	public Date getStart() {
		return m_start;
	}

	public void setStart(Date start) {
		m_start = start;
	}

	public Date getEnd() {
		return m_end;
	}

	public void setEnd(Date end) {
		m_end = end;
	}

	public List<PatternItem> getPattermItems() {
		return m_pattermItems;
	}

	public void setPattermItems(List<PatternItem> pattermItems) {
		m_pattermItems = pattermItems;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public List<String> getCities() {
		return m_cities;
	}

	public void setCities(List<String> cities) {
		m_cities = cities;
	}

	public List<LineChart> getLineCharts() {
		return new ArrayList<LineChart>(m_lineCharts.values());
	}

	public void setLineCharts(Map<String, LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public LineChart getLineChart() {
   	return m_lineChart;
   }

	public void setLineChart(LineChart lineChart) {
   	m_lineChart = lineChart;
   }

	public PieChart getPieChart() {
   	return m_pieChart;
   }

	public void setPieChart(PieChart pieChart) {
   	m_pieChart = pieChart;
   }
	
}
