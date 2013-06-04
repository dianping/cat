package com.dianping.cat.report.page.dependency;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.home.dependency.graph.entity.Node;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.dianping.cat.report.page.dependency.graph.LineGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.model.dependency.DependencyReportMerger;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private EventCollectManager m_manager;

	@Inject(type = ModelService.class, value = "dependency")
	private ModelService<DependencyReport> m_dependencyService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private TopologyGraphManager m_graphManager;

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_problemservice;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private Segment buildAllSegment(DependencyReport report) {
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

	private void buildGraphByEvent(TopologyGraph graph, Map<String, List<Event>> events) {
		for (Entry<String, List<Event>> entry : events.entrySet()) {
			List<Event> eventList = entry.getValue();

			for (Event event : eventList) {
				Node node = graph.findNode(event.getDomain());

				if (node != null) {
					String des = node.getDes();
					des = "</br>" + m_sdf.format(event.getDate()) + " " + event.getSubject();

					node.setDes(des);
				}
			}
		}
	}

	private void buildGraphExtraInfo(Payload payload, Model model, TopologyGraph graph) {
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
		Date reportTime = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute());
		Segment segment = report.findSegment(model.getMinute());

		model.setReport(report);
		model.setSegment(segment);

		if (payload.isAll()) {
			model.setSegment(buildAllSegment(report));
		}
		model.setEvents(queryDependencyEvent(segment, payload.getDomain(), reportTime));
	}

	private TopologyGraph buildHourlyTopologyGraph(Model model, Payload payload) {
		long time = payload.getDate() + TimeUtil.ONE_MINUTE * computeMinute(payload);
		String domain = payload.getDomain();

		return m_graphManager.buildGraphByDomainTime(domain, time);
	}

	private String buildLink(Payload payload, Model model, String domain) {
		return String.format("?op=graph&minute=%s&domain=%s&date=%s", model.getMinute(), domain,
		      m_dateFormat.format(new Date(payload.getDate())));
	}

	private String buildProblemInfo(String domain, Payload payload) {
		ProblemReport report = queryProblemReport(payload, domain);
		ExceptionInfoBuilder visitor = new ExceptionInfoBuilder();

		visitor.visitProblemReport(report);
		String result = visitor.buildResult();
		return result;
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
		DependencyReport report = queryDependencyReport(payload);
		switch (action) {
		case GRAPH:
			buildHourlyReport(report, model, payload);
			TopologyGraph graph = buildHourlyTopologyGraph(model, payload);

			buildGraphByEvent(graph, model.getEvents());
			buildGraphExtraInfo(payload, model, graph);
			model.setTopologyGraph(new DefaultJsonBuilder().buildJson(graph));
			break;
		case VIEW:
			buildHourlyReport(report, model, payload);
			buildHourlyLineGraph(report, model);
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

	private Map<String, List<Event>> queryDependencyEvent(Segment segment, String domain, Date date) {
		Map<String, List<Event>> result = new LinkedHashMap<String, List<Event>>();
		Map<String, List<String>> dependencies = parseDependencies(segment);
		List<Event> domainEvents = m_manager.queryEvents(domain, date);

		if (domainEvents != null && domainEvents.size() > 0) {
			result.put(domain, domainEvents);
		}
		for (Entry<String, List<String>> entry : dependencies.entrySet()) {
			String key = entry.getKey();
			List<String> targets = entry.getValue();

			for (String temp : targets) {
				List<Event> queryEvents = m_manager.queryEvents(temp, date);

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
		      .setProperty("date", date);
		if (m_problemservice.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemservice.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

}
