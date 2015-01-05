package com.dianping.cat.report.page.dependency;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class ExternalInfoBuilder {

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_problemservice;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	private ModelService<TopReport> m_topService;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private ExceptionConfigManager m_configManager;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

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

	public void buildNodeExceptionInfo(TopologyNode node, Model model, Payload payload) {
		String domain = node.getId();
		if (node.getStatus() != GraphConstrant.OK) {
			String exceptionInfo = buildProblemInfo(domain, payload);

			node.setDes(node.getDes() + exceptionInfo);
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

		List<String> excludeDomains = Arrays.asList(Constants.FRONT_END);
		TopMetric topMetric = new TopMetric(minuteCount, payload.getTopCounts(), m_configManager, excludeDomains);
		Date end = new Date(payload.getDate() + TimeHelper.ONE_MINUTE * minute);
		Date start = new Date(end.getTime() - TimeHelper.ONE_MINUTE * minuteCount);

		topMetric.setStart(start).setEnd(end);
		if (minuteCount > minute) {
			Payload lastPayload = new Payload();
			Date lastHour = new Date(payload.getDate() - TimeHelper.ONE_HOUR);
			lastPayload.setDate(new SimpleDateFormat("yyyyMMddHH").format(lastHour));

			topMetric.visitTopReport(queryTopReport(lastPayload));
		}
		report.accept(new TopExceptionExclude(m_configManager));
		topMetric.visitTopReport(report);
		model.setTopReport(report);
		model.setTopMetric(topMetric);
	}

	private String buildTopologyNodeLink(Payload payload, Model model, String domain) {
		return String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", model.getMinute(), domain,
		      m_dateFormat.format(new Date(payload.getDate())));
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
		String domain = Constants.CAT;
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = m_reportService.queryTopReport(domain, new Date(payload.getDate()), new Date(payload.getDate()
				      + TimeHelper.ONE_HOUR));
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}
}