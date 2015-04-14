package com.dianping.cat.report.page.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.url.pattern.entity.PatternItem;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.page.web.graph.WebGraphCreator;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private CityManager m_cityManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private WebGraphCreator m_graphCreator;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	private Pair<Map<String, LineChart>, List<PieChart>> buildDisplayInfo(QueryEntity query, String title) {
		Pair<Map<String, LineChart>, List<PieChart>> charts = m_graphCreator.queryBaseInfo(query, title);
		Map<String, LineChart> lineCharts = charts.getKey();
		List<PieChart> pieCharts = charts.getValue();

		return new Pair<Map<String, LineChart>, List<PieChart>>(lineCharts, pieCharts);
	}

	private void buildInfoCharts(Model model, QueryEntity currentQuery, QueryEntity compareQuery) {
		Map<String, LineChart> lineCharts = new LinkedHashMap<String, LineChart>();
		List<PieChart> pieCharts = new LinkedList<PieChart>();

		if (currentQuery != null) {
			Pair<Map<String, LineChart>, List<PieChart>> currentPair = buildDisplayInfo(currentQuery, "当前值");

			lineCharts.putAll(currentPair.getKey());
			pieCharts.addAll(currentPair.getValue());

			if (compareQuery != null) {
				Pair<Map<String, LineChart>, List<PieChart>> comparePair = buildDisplayInfo(compareQuery, "对比值");
				for (Entry<String, LineChart> entry : comparePair.getKey().entrySet()) {
					LineChart linechart = entry.getValue();
					LineChart l = lineCharts.get(entry.getKey());

					if (l != null) {
						l.add(linechart.getSubTitles().get(0), linechart.getValueObjects().get(0));
					}
				}
				pieCharts.addAll(comparePair.getValue());
				model.setCompareStart(compareQuery.getStart());
				model.setCompareEnd(compareQuery.getEnd());
			}
			for (Entry<String, LineChart> entry : lineCharts.entrySet()) {
				if (WebGraphCreator.SUCESS_PERCENT.equals(entry.getKey())) {
					LineChart linechart = entry.getValue();

					linechart.setMinYlable(linechart.queryMinYlable(linechart.getValueObjects()));
					linechart.setMaxYlabel(100.0);
				}
			}
		}
		model.setLineCharts(lineCharts);
		model.setPieCharts(pieCharts);
	}

	private Pair<QueryEntity, QueryEntity> buildQueryEntities(Payload payload) {
		Pair<Date, Date> startPair = payload.getHistoryStartDatePair();
		Pair<Date, Date> endPair = payload.getHistoryEndDatePair();
		QueryEntity current = null;
		QueryEntity compare = null;
		String url = payload.getUrl();

		if (url != null) {
			List<String> urls = Splitters.by(";").split(url);
			List<String> channels = Splitters.by(";").split(payload.getChannel());
			List<String> cities = Splitters.by(";").split(payload.getCity());
			String type = payload.getType();
			current = buildQueryEntity(startPair.getKey(), endPair.getKey(), urls.get(0), type, channels.get(0),
			      cities.get(0));

			if (startPair.getValue() != null && endPair.getValue() != null && urls.size() == 2) {
				compare = buildQueryEntity(startPair.getValue(), endPair.getValue(), urls.get(1), Constants.TYPE_INFO,
				      channels.get(1), cities.get(1));
			}
		}
		return new Pair<QueryEntity, QueryEntity>(current, compare);
	}

	private QueryEntity buildQueryEntity(Date start, Date end, String url, String type, String channel, String city) {
		QueryEntity queryEntity = new QueryEntity(start, end, url);

		queryEntity.addPar("metricType", Constants.METRIC_USER_MONITOR).addPar("type", type).addPar("channel", channel)
		      .addPar("city", city);
		return queryEntity;
	}

	private ProblemReport getHourlyReport(Payload payload) {
		ModelRequest request = new ModelRequest(Constants.FRONT_END, payload.getDate()) //
		      .setProperty("queryType", "view");
		if (!Constants.ALL.equals(payload.getIpAddress())) {
			request.setProperty("ip", payload.getIpAddress());
		}
		if (!StringUtils.isEmpty(payload.getType())) {
			request.setProperty("type", "error");
		}
		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			ProblemReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "web")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "web")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		Pair<QueryEntity, QueryEntity> queryEntities = buildQueryEntities(payload);
		QueryEntity currentQuery = queryEntities.getKey();
		QueryEntity compareQuery = queryEntities.getValue();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			try {
				if (currentQuery != null) {
					if (Constants.TYPE_INFO.equals(payload.getType())) {
						buildInfoCharts(model, currentQuery, compareQuery);
					} else {
						Pair<LineChart, PieChart> pair = m_graphCreator.queryErrorInfo(currentQuery);

						model.setLineChart(pair.getKey());
						model.setPieChart(pair.getValue());
					}
					model.setStart(currentQuery.getStart());
					model.setEnd(currentQuery.getEnd());
				} else {
					model.setStart(TimeHelper.getCurrentDay());
					model.setEnd(new Date());
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			model.setPattermItems(m_patternManager.queryUrlPatternRules());
			model.setAction(Action.VIEW);
			model.setCityInfo(m_cityManager.getCityInfo());
			break;
		case JSON:
			try {
				Map<String, Object> jsonObjs = new HashMap<String, Object>();
				if (currentQuery != null) {
					if (Constants.TYPE_INFO.equals(payload.getType())) {
						Pair<Map<String, LineChart>, List<PieChart>> currentPair = buildDisplayInfo(currentQuery, "当前值");

						jsonObjs.put("lineCharts", currentPair.getKey());
						jsonObjs.put("pieCharts", currentPair.getValue());
					} else {
						Pair<LineChart, PieChart> pair = m_graphCreator.queryErrorInfo(currentQuery);

						jsonObjs.put("lineChart", pair.getKey());
						jsonObjs.put("pieChart", pair.getValue());
					}
				}
				model.setJson(new JsonBuilder().toJson(jsonObjs));
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case PROBLEM:
			ProblemReport problemReport = getHourlyReport(payload);
			ProblemStatistics problemStatistics = new ProblemStatistics();
			String ip = payload.getIpAddress();

			if (ip.equals(Constants.ALL)) {
				problemStatistics.setAllIp(true);
			} else {
				problemStatistics.setIp(ip);
			}
			problemStatistics.visitProblemReport(problemReport);
			model.setProblemReport(problemReport);
			model.setAllStatistics(problemStatistics);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.WEB);
		model.setAction(payload.getAction());
		Collection<PatternItem> rules = m_patternManager.queryUrlPatternRules();
		String url = payload.getUrl();

		if (url == null && rules.size() > 0) {
			PatternItem patternItem = new ArrayList<PatternItem>(rules).get(0);

			url = patternItem.getName();
			payload.setGroup(patternItem.getGroup());
			payload.setUrl(url);
		}

		m_normalizePayload.normalize(model, payload);
	}

	public class QueryEntity {
		private String m_url;

		private Date m_start;

		private Date m_end;

		private Map<String, String> m_pars = new HashMap<String, String>();

		public QueryEntity(Date start, Date end, String url) {
			m_start = start;
			m_end = end;
			m_url = url;
		}

		public QueryEntity addPar(String par, String value) {
			m_pars.put(par, value);
			return this;
		}

		public Date getEnd() {
			return m_end;
		}

		public Map<String, String> getPars() {
			return m_pars;
		}

		public Date getStart() {
			return m_start;
		}

		public String getType() {
			return m_pars.get("type");
		}

		public String getUrl() {
			return m_url;
		}

		public QueryEntity setEnd(Date end) {
			m_end = end;
			return this;
		}

		public QueryEntity setStart(Date start) {
			m_start = start;
			return this;
		}
	}
}
