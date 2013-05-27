package com.dianping.cat.report.page.dependency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {

	private DependencyReport m_report;

	private Segment m_segment;

	private int m_minute;

	private List<Integer> m_minutes;

	private int m_maxMinute;

	private Map<String, List<Event>> m_events;

	private String m_graph;

	private List<String> m_indexGraph;

	private Map<String, List<String>> m_dependencyGraph;

	public List<String> getIndexGraph() {
		return m_indexGraph;
	}

	public void setIndexGraph(List<String> indexGraph) {
		m_indexGraph = indexGraph;
	}

	public Map<String, List<String>> getDependencyGraph() {
		return m_dependencyGraph;
	}

	public void setDependencyGraph(Map<String, List<String>> dependencyGraph) {
		m_dependencyGraph = dependencyGraph;
	}

	public Map<String, List<Event>> getEvents() {
		return m_events;
	}

	public void setEvents(Map<String, List<Event>> events) {
		m_events = events;
	}

	public List<Integer> getMinutes() {
		return m_minutes;
	}

	public void setMinutes(List<Integer> minutes) {
		m_minutes = minutes;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public int getMaxMinute() {
		return m_maxMinute;
	}

	public void setMaxMinute(int maxMinute) {
		m_maxMinute = maxMinute;
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

	public DependencyReport getReport() {
		return m_report;
	}

	public Segment getSegment() {
		return m_segment;
	}

	public void setReport(DependencyReport report) {
		m_report = report;
	}

	public void setSegment(Segment segment) {
		m_segment = segment;
	}

	public int getMinute() {
		return m_minute;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public String getGraph() {
		return m_graph;
	}

	public void setGraph(String graph) {
		m_graph = graph;
	}

}
