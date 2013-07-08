package com.dianping.cat.report.page.dependency;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.top.TopMetric;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class ExternalInfoBuilder {

	@Inject(type = ModelService.class, value = "problem")
	private ModelService<ProblemReport> m_problemservice;

	@Inject(type = ModelService.class, value = "top")
	private ModelService<TopReport> m_topService;
	
	@Inject 
	private EventCollectManager m_eventManager;
	
	@Inject
	private ReportService m_reportService;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");
	
	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	public void buildExceptionInfoOnGraph(Payload payload, Model model, TopologyGraph graph) {
   	if (graph.getStatus() != GraphConstrant.OK) {
   		String problemInfo = buildProblemInfo(graph.getId(), payload);
   
   		graph.setDes(graph.getDes() + problemInfo);
   	}
   	for (TopologyNode node : graph.getNodes().values()) {
   		node.setLink(buildTopologyNodeLink(payload, model, node.getId()));

   		if (node.getType().equals(GraphConstrant.PROJECT)) {
   			if (node.getStatus() != GraphConstrant.OK) {
   				String problemInfo = buildProblemInfo(node.getId(), payload);
   
   				node.setDes(node.getDes() + problemInfo);
   			}
   		}
   	}
   }

	public void buildZabbixErrorOnGraph(TopologyGraph graph, String zabbixHeader, Map<String, List<Event>> events) {
		Set<String> nodes = new HashSet<String>();
   	for (Entry<String, List<Event>> entry : events.entrySet()) {
   		List<Event> eventList = entry.getValue();
   
   		for (Event event : eventList) {
   			TopologyNode node = graph.findTopologyNode(event.getDomain());
   
   			if (node != null) {
   				if (!nodes.contains(node.getId())) {
   					node.setDes(node.getDes() + zabbixHeader);
   					nodes.add(node.getId());
   				}
   				if (node.getStatus() == GraphConstrant.OK) {
   					node.setStatus(GraphConstrant.OP_ERROR);
   				}
   				String des = node.getDes();
   			
   				des = des + m_sdf.format(event.getDate()) + " " + event.getSubject() + GraphConstrant.ENTER;
   				node.setDes(des);
   			} else if (event.getDomain().equals(graph.getId())) {
   				if (!nodes.contains(graph.getId())) {
   					graph.setDes(graph.getDes() + zabbixHeader);
   					nodes.add(graph.getId());
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

	private String buildTopologyNodeLink(Payload payload, Model model, String domain) {
   	return String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", model.getMinute(), domain,
   	      m_dateFormat.format(new Date(payload.getDate())));
   }

	public void buildNodeExceptionInfo(TopologyNode node, Model model, Payload payload) {
   	String domain = node.getId();
   	if (node.getStatus() != GraphConstrant.OK) {
   		String exceptionInfo = buildProblemInfo(domain, payload);
   
   		node.setDes(node.getDes() + exceptionInfo);
   	}
   }
	
	public void buildNodeZabbixInfo(TopologyNode node,Model model, Payload payload) {
		Date reportTime = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute());
   	String domain = node.getId();
   	List<Event> events = m_eventManager.findEvents(reportTime.getTime(), domain);
   	
   	node.setLink(buildTopologyNodeLink(payload, model, domain));
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
   }

	private String buildProblemInfo(String domain, Payload payload) {
   	ProblemReport report = queryProblemReport(payload, domain);
   	ProblemInfoVisitor visitor = new ProblemInfoVisitor();
   
   	visitor.visitProblemReport(report);
   	return visitor.buildExceptionInfo();
   }

	public void buildTopErrorInfo(Payload payload, Model model) {
		int minuteCount = payload.getMinuteCounts();
		int minute = model.getMinute();
		TopReport report = queryTopReport(payload);
		TopMetric topMetric = new TopMetric(minuteCount, payload.getTopCounts());
		Date end = new Date(payload.getDate() + TimeUtil.ONE_MINUTE * minute);
		Date start = new Date(end.getTime() - TimeUtil.ONE_MINUTE * minuteCount);

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			Payload lastPayload = new Payload();
			Date lastHour = new Date(payload.getDate() - TimeUtil.ONE_HOUR);
			lastPayload.setDate(new SimpleDateFormat("yyyyMMddHH").format(lastHour));

			topMetric.visitTopReport(queryTopReport(lastPayload));
		}
		topMetric.visitTopReport(report);
		model.setTopReport(report);
		model.setTopMetric(topMetric);
	}
	
	public String buildZabbixHeader(Payload payload, Model model) {
		StringBuilder sb = new StringBuilder();
		long end = payload.getDate() + TimeUtil.ONE_MINUTE * model.getMinute();

		sb.append(GraphConstrant.LINE).append(GraphConstrant.ENTER);
		sb.append("<span style='color:red'>").append(CatString.ZABBIX_ERROR).append("(")
		      .append(m_sdf.format(new Date(end - TimeUtil.ONE_MINUTE * 10))).append("-").append(m_sdf.format(end))
		      .append(")").append("</span>").append(GraphConstrant.ENTER);

		return sb.toString();
	}
	
	public Map<String, List<Event>> queryDependencyEvent(Map<String, List<String>> dependencies, String domain,
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

	private ProblemReport queryProblemReport(Payload payload, String domain) {
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("date", date).setProperty("type", "view");
		if (m_problemservice.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemservice.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	private TopReport queryTopReport(Payload payload) {
		String domain = CatString.CAT;
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();
			if (report == null || report.getDomains().size() == 0) {
				report = m_reportService.queryTopReport(domain, new Date(payload.getDate()), new Date(payload.getDate()
				      + TimeUtil.ONE_HOUR));
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}
}