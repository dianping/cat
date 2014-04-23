package com.dianping.cat.report.page.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.unidal.tuple.Pair;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.metric.Range;

@ModelMeta("network")
public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Collection<ProductLine> m_productLines;

	private Collection<MetricAggregationGroup> m_metricAggregationGroup;

	private Date m_startTime;

	private Date m_endTime;

	private ArrayList<Pair<String, String>> m_topoData;

	public Model(Context ctx) {
		super(ctx);
	}

	public Range[] getAllRange() {
		return Range.values();
	}

	@Override
	public Action getDefaultAction() {
		return Action.NETTOPOLOGY;
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

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}

	public Collection<MetricAggregationGroup> getMetricAggregationGroup() {
		return m_metricAggregationGroup;
	}

	public void setMetricAggregationGroup(Collection<MetricAggregationGroup> metricAggregationGroup) {
		m_metricAggregationGroup = metricAggregationGroup;
	}

	public Collection<ProductLine> getProductLines() {
		return m_productLines;
	}

	public void setProductLines(Collection<ProductLine> productLines) {
		m_productLines = productLines;
	}

	public ArrayList<Pair<String, String>> getTopoData() {
		return m_topoData;
	}

	public void setTopoData(ArrayList<Pair<String, String>> topoData) {
		m_topoData = topoData;
	}

}
