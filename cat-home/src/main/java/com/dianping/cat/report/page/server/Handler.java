package com.dianping.cat.report.page.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.server.transform.DefaultJsonParser;
import com.dianping.cat.alarm.server.transform.DefaultSaxParser;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;
import com.dianping.cat.alarm.spi.decorator.ServerRuleFTLDecorator;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;
import com.dianping.cat.home.server.entity.Group;
import com.dianping.cat.home.server.entity.ServerMetricConfig;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.server.config.ServerMetricConfigManager;
import com.dianping.cat.report.page.server.display.LineChartBuilder;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.report.page.server.service.MetricGraphBuilder;
import com.dianping.cat.report.page.server.service.MetricGraphService;
import com.dianping.cat.report.page.server.service.MetricScreenService;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private MetricScreenService m_screenService;

	@Inject
	private MetricGraphService m_graphService;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private MetricService m_metricService;

	@Inject
	private DataExtractor m_dataExtractor;

	@Inject
	private MetricGraphBuilder m_graphBuilder;

	@Inject
	private LineChartBuilder m_linechartBuilder;

	@Inject
	private InfluxDBConfigManager m_influxDBConfigManager;

	@Inject
	private ServerMetricConfigManager m_serverMetricConfigManager;

	@Inject
	private ServerAlarmRuleService m_ruleService;

	@Inject
	private ServerRuleFTLDecorator m_ftlDecorator;

	@Inject
	private NetGraphConfigManager m_netGraphConfigManager;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private Graph buildGraph(Payload payload) {
		List<String> endPoints = payload.getEndPoints();
		List<String> measurements = payload.getMeasurements();

		return m_graphBuilder.buildGraph(endPoints, measurements, String.valueOf(payload.getGraphId()), "");
	}

	private List<LineChart> buildGraphLinecharts(Model model, Payload payload, Date start, Date end) {
		List<LineChart> linecharts = new LinkedList<LineChart>();

		try {
			Graph graph = m_graphService.queryByGraphId(payload.getGraphId());
			graph = convertGraphType(graph, payload.getType());

			if (graph != null) {
				linecharts = m_linechartBuilder
				      .buildLineCharts(start, end, payload.getInterval(), payload.getView(), graph);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return linecharts;
	}

	private List<LineChart> buildScreenLinecharts(String screen, String interval, Date start, Date end) {
		List<LineChart> lineCharts = new LinkedList<LineChart>();

		try {
			Map<String, MetricScreenInfo> graphs = m_screenService.queryByName(screen);

			for (MetricScreenInfo info : graphs.values()) {
				Graph g = info.getGraph();

				if (g != null) {
					List<LineChart> lines = m_linechartBuilder.buildLineCharts(start, end, interval, "", g);

					lineCharts.addAll(lines);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineCharts;
	}

	private List<LineChart> buildViewLinecharts(Payload payload, Date start, Date end) {
		List<LineChart> lineCharts = new LinkedList<LineChart>();

		try {
			ServerMetricConfig config = m_serverMetricConfigManager.getConfig();
			Group group = config.getGroups().get(payload.getCategory());

			if (group != null) {
				String gName = payload.getGroup();
				List<com.dianping.cat.home.server.entity.Item> items = new LinkedList<com.dianping.cat.home.server.entity.Item>();

				if (StringUtils.isNotEmpty(gName)) {
					com.dianping.cat.home.server.entity.Item item = group.getItems().get(gName);

					if (item != null) {
						items.add(item);
					}
				} else {
					items = new LinkedList<com.dianping.cat.home.server.entity.Item>(group.getItems().values());
				}

				lineCharts = m_linechartBuilder.buildLineCharts(start, end, payload.getInterval(), payload.getEndPoint(),
				      items);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineCharts;
	}

	private Graph convertGraphType(Graph graph, String type) {
		for (Entry<String, Item> entry : graph.getItems().entrySet()) {
			for (Entry<String, Segment> e : entry.getValue().getSegments().entrySet()) {
				Segment segment = e.getValue();

				segment.setType(type);
			}
		}
		return graph;
	}

	private String generateRuleConfigContent(ServerAlarmRule rule) {
		String configsStr = "";

		if (rule != null) {
			try {
				ServerAlarmRuleConfig config = DefaultSaxParser.parse(rule.getContent());
				configsStr = new DefaultJsonBuilder(true).buildArray(config.getRules());
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		String content = m_ftlDecorator.generateConfigsHtml(configsStr);

		return content;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "server")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "server")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(payload, model);

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		Map<String, Object> JsonDatas = new HashMap<String, Object>();

		switch (action) {
		case VIEW:
			normalizeEndPoints(model, payload);
			normalizeGraphInfo(payload, model);

			List<LineChart> lineCharts = buildViewLinecharts(payload, start, end);

			model.setLineCharts(lineCharts);
			model.setServerMetricConfig(m_serverMetricConfigManager.getConfig());
			break;
		case VIEW_JSON:
			normalizeGraphInfo(payload, model);
			lineCharts = buildViewLinecharts(payload, start, end);

			JsonDatas.put("lineCharts", lineCharts);
			model.setJson(m_jsonBuilder.toJson(JsonDatas));
			break;
		case GRAPH:
			normalizeGraphInfo(payload, model);
			lineCharts = buildGraphLinecharts(model, payload, start, end);

			model.setLineCharts(lineCharts);
			break;
		case GRAPH_JSON:
			normalizeGraphInfo(payload, model);
			lineCharts = buildGraphLinecharts(model, payload, start, end);

			JsonDatas.put("lineCharts", lineCharts);
			model.setJson(m_jsonBuilder.toJson(JsonDatas));
			break;
		case SCREEN:
			normalizeScreenInfo(payload, model);
			lineCharts = buildScreenLinecharts(payload.getScreen(), payload.getInterval(), start, end);

			model.setLineCharts(lineCharts);
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case SCREEN_JSON:
			normalizeScreenInfo(payload, model);
			lineCharts = buildScreenLinecharts(payload.getScreen(), payload.getInterval(), start, end);

			JsonDatas.put("lineCharts", lineCharts);
			model.setJson(m_jsonBuilder.toJson(JsonDatas));
			break;
		case SCREENS:
			Map<String, Map<String, MetricScreenInfo>> screens = m_screenService.queryScreens();

			model.setMetricScreenInfos(screens);
			break;
		case SCREEN_UPDATE:
			String screen = payload.getScreen();

			if (StringUtils.isNotEmpty(screen)) {
				Map<String, MetricScreenInfo> infos = m_screenService.queryByName(screen);

				model.setGraphs(infos.keySet());
			}
			break;
		case SCREEN_DELETE:
			m_screenService.deleteByScreen(payload.getScreen());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case SCREEN_SUBMIT:
			m_screenService.updateScreen(payload.getScreen(), payload.getGraphs());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case GRAPH_UPDATE:
			MetricScreenInfo screenInfo = m_screenService.queryByNameGraph(payload.getScreen(), payload.getGraph());

			model.setMetricScreenInfo(screenInfo);
			break;
		case GRAPH_SUBMIT:
			m_screenService.insertOrUpdateGraph(payload.getGraphParam());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case AGGREGATE:
			break;
		case ENDPOINT:
			List<String> keywords = payload.getKeywordsList();

			if (!keywords.isEmpty()) {
				Set<String> endPoints = queryEndPoints(payload.getSearch(), keywords);
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("endPoints", endPoints);
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		case MEASUREMTN:
			List<String> endPoints = payload.getEndPoints();

			if (!endPoints.isEmpty()) {
				List<String> measurements = queryMeasurements(endPoints);
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("endPoints", measurements);
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		case BUILDVIEW:
			Graph graph = buildGraph(payload);
			boolean success = m_graphService.insert(graph);

			if (success) {
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("id", payload.getGraphId());
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		case INFLUX_CONFIG_UPDATE:
			String content = payload.getContent();

			if (!StringUtils.isEmpty(content)) {
				model.setOpState(m_influxDBConfigManager.insert(content));
			}
			model.setConfig(m_configHtmlParser.parse(m_influxDBConfigManager.getConfig().toString()));
			break;
		case SERVER_METRIC_CONFIG_UPDATE:
			content = payload.getContent();

			if (!StringUtils.isEmpty(content)) {
				model.setOpState(m_serverMetricConfigManager.insert(content));
			}
			model.setConfig(m_configHtmlParser.parse(m_serverMetricConfigManager.getConfig().toString()));
			break;
		case SERVER_ALARM_RULE:
			String type = payload.getType();
			List<ServerAlarmRule> rules = m_ruleService.queryRules(type);

			model.setServerAlarmRules(rules);
			break;
		case SERVER_ALARM_RULE_UPDATE:
			int id = payload.getRuleId();
			ServerAlarmRule rule = m_ruleService.queryById(id);

			model.setServerAlarmRule(rule);
			model.setContent(generateRuleConfigContent(rule));
			break;
		case SERVER_ALARM_RULE_DELETE:
			rule = new ServerAlarmRule();

			rule.setId(payload.getRuleId());
			model.setOpState(m_ruleService.delete(rule));
			break;
		case SERVER_ALARM_RULE_SUBMIT:
			model.setOpState(submitServerRule(payload));
			model.setServerAlarmRules(m_ruleService.queryRules(payload.getType()));
			break;
		case NET_GRAPH_CONFIG_UPDATE:
			String netGraphConfig = payload.getContent();
			if (!StringUtils.isEmpty(netGraphConfig)) {
				model.setOpState(m_netGraphConfigManager.insert(netGraphConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_netGraphConfigManager.getConfig().toString()));
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Payload payload, Model model) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.SERVER);
		m_normalizePayload.normalize(model, payload);
	}

	private void normalizeEndPoints(Model model, Payload payload) {
		Map<String, List<String>> eps = queryEndpoints();

		if (StringUtils.isEmpty(payload.getEndPoint())) {
			Entry<String, List<String>> catgy = eps.entrySet().iterator().next();

			payload.setCategory(catgy.getKey());

			if (!catgy.getValue().isEmpty()) {
				payload.setEndPoint(catgy.getValue().get(0));
			}
		}
		model.setEndPoints(eps);
	}

	private void normalizeGraphInfo(Payload payload, Model model) {
		if (StringUtils.isEmpty(payload.getInterval())) {
			long end = payload.getHistoryEndDate().getTime();
			long start = payload.getHistoryStartDate().getTime();
			int length = (int) ((end - start) / TimeHelper.ONE_MINUTE);
			int gap = m_dataExtractor.calculateInterval(length);

			payload.setInterval(gap + "m");
		}

		if (payload.getGraphId() == 0) {
			payload.setGraphId(m_graphService.getLast().getGraphId());
		}
	}

	private void normalizeScreenInfo(Payload payload, Model model) {
		normalizeGraphInfo(payload, model);

		Map<String, Map<String, MetricScreenInfo>> screenGroups = m_screenService.queryScreens();

		if (StringUtils.isEmpty(payload.getScreen())) {
			if (!screenGroups.isEmpty()) {
				String defaultScreen = screenGroups.keySet().iterator().next();

				payload.setScreen(defaultScreen);
			} else {
				payload.setScreen("");
			}
		}
	}

	private Map<String, List<String>> queryEndpoints() {
		Map<String, List<String>> results = new LinkedHashMap<String, List<String>>();

		for (String category : m_serverMetricConfigManager.getConfig().getGroups().keySet()) {
			List<String> endPoints = m_metricService.queryEndPoints(category);

			Collections.sort(endPoints);
			results.put(category, endPoints);
		}
		return results;
	}

	public Set<String> queryEndPoints(String search, List<String> keywords) {
		Set<String> endPoints = new HashSet<String>();
		Set<String> keySet = m_influxDBConfigManager.getConfig().getInfluxdbs().keySet();

		if (Constants.END_POINT.equals(search)) {
			for (String key : keySet) {
				endPoints.addAll(m_metricService.queryEndPoints(key, search, keywords));
			}
		} else {
			for (String key : keySet) {
				endPoints.addAll(m_metricService.queryEndPointsByTag(key, keywords));
			}
		}

		return endPoints;
	}

	private List<String> queryMeasurements(List<String> endPoints) {
		List<String> measurements = new ArrayList<String>();

		for (String key : m_influxDBConfigManager.getConfig().getInfluxdbs().keySet()) {
			List<String> measures = m_metricService.queryMeasurements(key, endPoints);

			measurements.addAll(m_linechartBuilder.parseSeries(measures));
		}

		Collections.sort(measurements);
		return measurements;
	}

	private boolean submitServerRule(Payload payload) {
		try {
			ServerAlarmRule rule = payload.getRule();
			List<Rule> ruleConfigs = DefaultJsonParser.parseArray(Rule.class, payload.getContent());
			ServerAlarmRuleConfig ruleConfig = new ServerAlarmRuleConfig();

			ruleConfig.setId(rule.getMeasurement());

			for (Rule config : ruleConfigs) {
				ruleConfig.addRule(config);
			}
			rule.setCategory(payload.getType());
			rule.setContent(ruleConfig.toString());

			ServerAlarmRule r = m_ruleService.queryById(rule.getId());

			if (r != null) {
				return m_ruleService.update(rule);
			} else {
				return m_ruleService.insert(rule);
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}
}