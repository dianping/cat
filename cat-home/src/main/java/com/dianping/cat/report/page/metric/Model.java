package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

@ModelMeta(MetricAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Collection<ProductLine> m_productLines;

	private List<String> m_metricGroups;

	private List<String> m_tags;

	private Date m_startTime;

	private Date m_endTime;

	private String m_json;

	public Model(Context ctx) {
		super(ctx);
	}

	public Range[] getAllRange() {
		return Range.values();
	}

	@Override
	public Action getDefaultAction() {
		return Action.METRIC;
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

	public String getJson() {
		return m_json;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public List<String> getMetricGroups() {
		return m_metricGroups;
	}

	public Collection<ProductLine> getProductLines() {
		return m_productLines;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public List<String> getTags() {
		return m_tags;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setJson(String json) {
		m_json = json;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setMetricGroups(List<String> metricGroups) {
		m_metricGroups = metricGroups;
	}

	public void setProductLines(Collection<ProductLine> productLines) {
		m_productLines = productLines;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}

	public void setTags(List<String> tags) {
		m_tags = tags;
	}

}
