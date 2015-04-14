package com.dianping.cat.report.page.cdn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	private List<String> m_cities;

	private Map<String, LineChart> m_lineCharts;

	private Date m_start;

	private Date m_end;

	private String m_cityInfo;

	public Model(Context ctx) {
		super(ctx);
	}

	public List<String> getCities() {
		return m_cities;
	}

	public String getCityInfo() {
		return m_cityInfo;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public Date getEnd() {
		return m_end;
	}

	public List<LineChart> getLineCharts() {
		if (m_lineCharts != null) {
			return new ArrayList<LineChart>(m_lineCharts.values());
		} else {
			return new ArrayList<LineChart>();
		}
	}

	public Date getStart() {
		return m_start;
	}

	public void setCities(List<String> cities) {
		m_cities = cities;
	}

	public void setCityInfo(String cityInfo) {
		m_cityInfo = cityInfo;
	}

	public void setEnd(Date end) {
		m_end = end;
	}

	public void setLineCharts(Map<String, LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setStart(Date start) {
		m_start = start;
	}

}
