package com.dianping.cat.report.page.system;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;

@ModelMeta("system")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private List<String> m_productLines;

	private Collection<String> m_ipAddrs;

	private List<String> m_metricGroups;

	private Date m_startTime;

	private Date m_endTime;

	public Model(Context ctx) {
		super(ctx);
	}

	public Range[] getAllRange() {
		return Range.values();
	}

	@Override
	public Action getDefaultAction() {
		return Action.SYSTEM;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public Collection<String> getIpAddrs() {
		return m_ipAddrs;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public List<String> getMetricGroups() {
		return m_metricGroups;
	}

	public List<String> getProductLines() {
		return m_productLines;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setIpAddrs(Collection<String> ipAddrs) {
		m_ipAddrs = ipAddrs;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setMetricGroups(List<String> metricGroups) {
		m_metricGroups = metricGroups;
	}

	public void setProductLines(List<String> productLines) {
		m_productLines = productLines;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}
}
