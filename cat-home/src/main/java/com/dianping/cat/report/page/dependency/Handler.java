package com.dianping.cat.report.page.dependency;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.home.dependency.graph.entity.Edge;
import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.dependency.dashboard.DashboardGraph;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.dianping.cat.report.page.dependency.graph.LineGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.model.dependency.DependencyReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_dependencyService;

	@Inject
	private EventCollectManager m_eventManager;

	@Inject
	private TopologyGraphManager m_graphManager;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_problemservice;
	
	private Set<String> m_nodes;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private Segment buildAllSegmentInfo(DependencyReport report) {
		Segment result = new Segment();
		Map<Integer, Segment> segments = report.getSegments();
		DependencyReportMerger merger = new DependencyReportMerger(null);

		for (Segment segment : segments.values()) {
			Map<String, Dependency> dependencies = segment.getDependencies();
			Map<String, Index> indexs = segment.getIndexs();

			for (Index index : indexs.values()) {
				Index temp = result.findOrCreateIndex(index.getName());
				merger.mergeIndex(temp, index);
			}
			for (Dependency dependency : dependencies.values()) {
				Dependency temp = result.findOrCreateDependency(dependency.getKey());

				merger.mergeDependency(temp, dependency);
			}
		}
		return result;
	}

	private void buildDashboardErrorInfo(DashboardGraph dashboardGraph, Model model, Payload payload) {
		Map<String, List<Node>> nodes = dashboardGraph.getNodes();
		Date reportTime = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute());

		for (Entry<String, List<Node>> entry : nodes.entrySet()) {
			for (Node node : entry.getValue()) {
				String domain = node.getId();
				List<Event> events = m_eventManager.queryEvents(domain, reportTime);

				node.setLink(buildLink(payload, model, domain));

				if (events != null && events.size() > 0) {
					if (node.getStatus() == GraphConstrant.OK) {
						node.setStatus(GraphConstrant.OP_ERROR);
					}
					node.setDes(node.getDes() + buildZabbixHeader(payload, model));

					StringBuilder sb = new StringBuilder();
					for (Event event : events) {
						sb.append(m_sdf.format(event.getDate())).append((" "));
						sb.append(event.getSubject()).append(GraphConstrant.ENTER);
					}
					node.setDes(node.getDes() + sb.toString());
				}
				if (node.getStatus() != GraphConstrant.OK) {
					String exceptionInfo = buildProblemInfo(domain, payload);

					node.setDes(node.getDes() + exceptionInfo);
				}
			}
		}
	}

	private void buildExceptionInfoOnGraph(Payload payload, Model model, TopologyGraph graph) {
		if (graph.getStatus() != GraphConstrant.OK) {
			String problemInfo = buildProblemInfo(graph.getId(), payload);

			graph.setDes(graph.getDes() + problemInfo);
		}
		for (Node node : graph.getNodes().values()) {
			if (node.getType().equals(GraphConstrant.PROJECT)) {
				node.setLink(buildLink(payload, model, node.getId()));

				if (node.getStatus() != GraphConstrant.OK) {
					String problemInfo = buildProblemInfo(node.getId(), payload);

					node.setDes(node.getDes() + problemInfo);
				}
			}
		}
	}

	private void buildExternalErrorOnGraph(TopologyGraph graph, String zabbixHeader, Map<String, List<Event>> events) {
		for (Entry<String, List<Event>> entry : events.entrySet()) {
			List<Event> eventList = entry.getValue();

			for (Event event : eventList) {
				Node node = graph.findNode(event.getDomain());

				if (node != null) {
					if (!m_nodes.contains(node.getId())) {
						node.setDes(node.getDes() + zabbixHeader);
						m_nodes.add(node.getId());
					}
					if (node.getStatus() == GraphConstrant.OK) {
						node.setStatus(GraphConstrant.OP_ERROR);
					}

					String des = node.getDes();
					des = des + m_sdf.format(event.getDate()) + " " + event.getSubject() + GraphConstrant.ENTER;

					node.setDes(des);
				} else if (event.getDomain().equals(graph.getId())) {
					if (!m_nodes.contains(graph.getId())) {
						graph.setDes(graph.getDes() + zabbixHeader);
						m_nodes.add(graph.getId());
					}
					if (graph.getStatus() == GraphConstrant.OK) {
						graph.setStatus(GraphConstrant.OP_ERROR);
					}

					String des = graph.getDes();
					des = des + m_sdf.format(event.getDate()) + " " + event.getSubject() + GraphConstrant.ENTER;

					graph.setDes(des);
				}
			}
		}
	}

	private List<String> buildGraphList(List<LineChart> charts) {
		List<String> result = new ArrayList<String>();

		for (LineChart temp : charts) {
			result.add(temp.getJsonString());
		}
		return result;
	}

	private Map<String, List<String>> buildGraphMap(Map<String, List<LineChart>> charts) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();

		for (Entry<String, List<LineChart>> temp : charts.entrySet()) {
			result.put(temp.getKey(), buildGraphList(temp.getValue()));
		}
		return result;
	}

	private void buildHourlyLineGraph(DependencyReport report, Model model) {
		LineGraphBuilder builder = new LineGraphBuilder();

		builder.visitDependencyReport(report);

		List<LineChart> index = builder.queryIndex();
		Map<String, List<LineChart>> dependencys = builder.queryDependencyGraph();

		model.setIndexGraph(buildGraphList(index));
		model.setDependencyGraph(buildGraphMap(dependencys));
	}

	private void buildHourlyReport(DependencyReport report, Model model, Payload payload) {
		Segment segment = report.findSegment(model.getMinute());

		model.setReport(report);
		model.setSegment(segment);

		if (payload.isAll()) {
			model.setSegment(buildAllSegmentInfo(report));
		}
	}

	private String buildLink(Payload payload, Model model, String domain) {
		return String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", model.getMinute(), domain,
		      m_dateFormat.format(new Date(payload.getDate())));
	}

	private String buildProblemInfo(String domain, Payload payload) {
		ProblemReport report = queryProblemReport(payload, domain);
		ExceptionInfoBuilder visitor = new ExceptionInfoBuilder();

		visitor.visitProblemReport(report);
		String result = visitor.buildResult();
		return result;
	}

	private String buildZabbixHeader(Payload payload, Model model) {
		StringBuilder sb = new StringBuilder();
		long end = payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute();

		sb.append(GraphConstrant.LINE).append(GraphConstrant.ENTER);
		sb.append("<span style='color:red'>").append(CatString.ZABBIX_ERROR).append("(")
		      .append(m_sdf.format(new Date(end - TimeUtil.ONE_MINUTE * 10))).append("-").append(m_sdf.format(end))
		      .append(")").append("</span>").append(GraphConstrant.ENTER);

		return sb.toString();
	}

	private int computeMinute(Payload payload) {
		int minute = 0;
		String min = payload.getMinute();

		if (StringUtil.isEmpty(min)) {
			long current = System.currentTimeMillis() / 1000 / 60;
			minute = (int) (current % (60));
		} else {
			minute = Integer.parseInt(min);
		}

		return minute;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "dependency")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "dependency")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);

		Action action = payload.getAction();
		Date reportTime = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute());
		DependencyReport report = queryDependencyReport(payload);
		switch (action) {
		case GRAPH:
			TopologyGraph topologyGraph = m_graphManager.buildGraphByDomainTime(model.getDomain(), reportTime.getTime());
			Map<String, List<String>> graphDependency = parseDependencies(topologyGraph);
			Map<String, List<Event>> externalErrors = queryDependencyEvent(graphDependency, model.getDomain(), reportTime);

			buildHourlyReport(report, model, payload);
			model.setEvents(externalErrors);
			m_nodes = new HashSet<String>();
			buildExternalErrorOnGraph(topologyGraph, buildZabbixHeader(payload, model), externalErrors);
			buildExceptionInfoOnGraph(payload, model, topologyGraph);
			model.setTopologyGraph(new DefaultJsonBuilder().buildJson(topologyGraph));
			break;
		case VIEW:
			buildHourlyReport(report, model, payload);
			buildHourlyLineGraph(report, model);

			Segment segment = report.findSegment(model.getMinute());
			Map<String, List<String>> dependency = parseDependencies(segment);

			model.setEvents(queryDependencyEvent(dependency, model.getDomain(), reportTime));
			break;
		case DASHBOARD:
			DashboardGraph dashboardGraph = m_graphManager.buildDashboardGraph(reportTime.getTime());

			buildDashboardErrorInfo(dashboardGraph, model, payload);
			model.setDashboardGraph(dashboardGraph.toJson());
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.DEPENDENCY);
		model.setAction(Action.VIEW);

		m_normalizePayload.normalize(model, payload);

		Integer minute = computeMinute(payload);
		int maxMinute = 60;
		List<Integer> minutes = new ArrayList<Integer>();

		if (payload.getPeriod().isCurrent()) {
			long current = System.currentTimeMillis() / 1000 / 60;
			maxMinute = (int) (current % (60));
		}
		for (int i = 0; i < 60; i++) {
			minutes.add(i);
		}
		model.setMinute(minute);
		model.setMaxMinute(maxMinute);
		model.setMinutes(minutes);
	}

	private Map<String, List<String>> parseDependencies(Segment segment) {
		Map<String, List<String>> results = new TreeMap<String, List<String>>();
		if (segment != null) {
			Map<String, Dependency> dependencies = segment.getDependencies();

			for (Dependency temp : dependencies.values()) {
				String type = temp.getType();
				String target = temp.getTarget();
				List<String> targets = results.get(type);

				if (targets == null) {
					targets = new ArrayList<String>();
					results.put(type, targets);
				}
				targets.add(target);
			}
		}

		return results;
	}

	private Map<String, List<String>> parseDependencies(TopologyGraph graph) {
		Map<String, List<String>> dependencies = new HashMap<String, List<String>>();
		Map<String, Edge> edges = graph.getEdges();

		for (Edge temp : edges.values()) {
			String type = temp.getType();
			String target = temp.getTarget();

			List<String> targets = dependencies.get(type);
			if (targets == null) {
				targets = new ArrayList<String>();
				dependencies.put(type, targets);
			}
			targets.add(target);
		}
		return dependencies;
	}

	private Map<String, List<Event>> queryDependencyEvent(Map<String, List<String>> dependencies, String domain,
	      Date date) {
		Map<String, List<Event>> result = new LinkedHashMap<String, List<Event>>();
		List<Event> domainEvents = m_eventManager.queryEvents(domain, date);

		if (domainEvents != null && domainEvents.size() > 0) {
			result.put(domain, domainEvents);
		}
		for (Entry<String, List<String>> entry : dependencies.entrySet()) {
			String key = entry.getKey();
			List<String> targets = entry.getValue();

			for (String temp : targets) {
				List<Event> queryEvents = m_eventManager.queryEvents(temp, date);

				if (queryEvents != null && queryEvents.size() > 0) {
					List<Event> events = result.get(key);

					if (events == null) {
						events = new ArrayList<Event>();
						result.put(key, events);
					}
					events.addAll(queryEvents);
				}
			}
		}
		return result;
	}

	private DependencyReport queryDependencyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_dependencyService.isEligable(request)) {
			ModelResponse<DependencyReport> response = m_dependencyService.invoke(request);
			DependencyReport report = response.getModel();

			if (report != null && report.getStartTime() == null) {
				report.setStartTime(new Date(payload.getDate()));
				report.setStartTime(new Date(payload.getDate() + TimeUtil.ONE_HOUR));
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable dependency service registered for " + request + "!");
		}
	}

	private ProblemReport queryProblemReport(Payload payload, String domain) {
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date).setProperty("type", "view");
		if (m_problemservice.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemservice.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

}
