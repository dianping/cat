package com.dianping.cat.report.page.network;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.unidal.tuple.Pair;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;

@ModelMeta("network")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Collection<ProductLine> m_productLines;

	private Date m_startTime;

	private Date m_endTime;

	private int m_minute;

	private int m_maxMinute;

	private List<Integer> m_minutes;

	private List<Pair<String, String>> m_netGraphData;

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

	public int getMaxMinute() {
		return m_maxMinute;
	}

	public int getMinute() {
		return m_minute;
	}

	public List<Integer> getMinutes() {
		return m_minutes;
	}

	public List<Pair<String, String>> getNetGraphData() {
		return m_netGraphData;
	}

	public Collection<ProductLine> getProductLines() {
		return m_productLines;
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

	public void setMaxMinute(int maxMinute) {
		m_maxMinute = maxMinute;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public void setMinutes(List<Integer> minutes) {
		m_minutes = minutes;
	}

	public void setNetGraphData(List<Pair<String, String>> netGraphData) {
		m_netGraphData = netGraphData;
	}

	public void setProductLines(Collection<ProductLine> productLines) {
		m_productLines = productLines;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}

}
