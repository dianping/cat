package com.dianping.cat.report.page.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.dependency.dashboard.ProductLinesDashboard;
import com.dianping.cat.report.page.top.TopMetric;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta(DependencyAnalyzer.ID)
public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	private DependencyReport m_report;
	
	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Segment m_segment;

	private int m_minute;

	private List<Integer> m_minutes;

	private int m_maxMinute;

	private Map<String, List<Event>> m_events;

	private String m_topologyGraph;

	private List<String> m_indexGraph;

	private Map<String, List<String>> m_dependencyGraph;

	private String m_dashboardGraph;

	private ProductLinesDashboard m_dashboardGraphData;

	private List<ProductLine> m_productLines;

	private String m_productLineGraph;

	private Date m_reportStart;

	private Date m_reportEnd;

	private TopMetric m_topMetric;

	private TopReport m_topReport;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDashboardGraph() {
		return m_dashboardGraph;
	}

	public ProductLinesDashboard getDashboardGraphData() {
		return m_dashboardGraphData;
	}

	@Override
	public Action getDefaultAction() {
		return Action.LINE_CHART;
	}

	public Map<String, List<String>> getDependencyGraph() {
		return m_dependencyGraph;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public Map<String, List<Event>> getEvents() {
		return m_events;
	}

	public List<String> getIndexGraph() {
		return m_indexGraph;
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

	public String getProductLineGraph() {
		return m_productLineGraph;
	}

	public List<ProductLine> getProductLines() {
		return m_productLines;
	}

	public DependencyReport getReport() {
		return m_report;
	}

	public Date getReportEnd() {
		return m_reportEnd;
	}

	public Date getReportStart() {
		return m_reportStart;
	}

	public Segment getSegment() {
		return m_segment;
	}

	public TopMetric getTopMetric() {
		return m_topMetric;
	}

	public String getTopologyGraph() {
		return m_topologyGraph;
	}

	public TopReport getTopReport() {
		return m_topReport;
	}

	public void setDashboardGraph(String dashboardGraph) {
		m_dashboardGraph = dashboardGraph;
	}

	public void setDashboardGraphData(ProductLinesDashboard dashboardGraphData) {
		m_dashboardGraphData = dashboardGraphData;
	}

	public void setDependencyGraph(Map<String, List<String>> dependencyGraph) {
		m_dependencyGraph = dependencyGraph;
	}

	public void setEvents(Map<String, List<Event>> events) {
		m_events = events;
	}

	public void setIndexGraph(List<String> indexGraph) {
		m_indexGraph = indexGraph;
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

	public void setProductLineGraph(String productLineGraph) {
		m_productLineGraph = productLineGraph;
	}

	public void setProductLines(List<ProductLine> productLines) {
		m_productLines = productLines;
	}

	public void setReport(DependencyReport report) {
		m_report = report;
	}

	public void setReportEnd(Date reportEnd) {
		m_reportEnd = reportEnd;
	}

	public void setReportStart(Date reportStart) {
		m_reportStart = reportStart;
	}

	public void setSegment(Segment segment) {
		m_segment = segment;
	}

	public void setTopMetric(TopMetric topMetric) {
		m_topMetric = topMetric;
	}

	public void setTopologyGraph(String topologyGraph) {
		m_topologyGraph = topologyGraph;
	}

	public void setTopReport(TopReport topReport) {
		m_topReport = topReport;
	}

}
