package com.dianping.cat.report.page.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.display.ChartSorter;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.page.web.graph.WebGraphCreator;
import com.dianping.cat.report.page.web.service.WebApiQueryEntity;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private CityManager m_cityManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private WebGraphCreator m_graphCreator;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "web")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	private LineChart buildLineChart(Payload payload) {
		WebApiQueryEntity entity1 = payload.getQueryEntity1();
		WebApiQueryEntity entity2 = payload.getQueryEntity2();
		String type = payload.getType();
		LineChart lineChart = new LineChart();

		try {
			lineChart = m_graphCreator.buildLineChart(entity1, entity2, type);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineChart;
	}

	private Pair<PieChart, List<PieChartDetailInfo>> buildPieChart(Payload payload) {
		try {
			Pair<PieChart, List<PieChartDetailInfo>> pair = m_graphCreator.buildPieChart(payload.getQueryEntity1(),
			      payload.getGroupByField());
			List<PieChartDetailInfo> infos = pair.getValue();
			Collections.sort(infos, new ChartSorter().buildPieChartInfoComparator());

			return pair;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	@OutboundActionMeta(name = "web")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		switch (action) {
		case VIEW:
			LineChart lineChart = buildLineChart(payload);
			model.setLineChart(lineChart);
			break;
		case PIECHART:
			Pair<PieChart, List<PieChartDetailInfo>> pieChartPair = buildPieChart(payload);

			if (pieChartPair != null) {
				model.setPieChart(pieChartPair.getKey());
				model.setPieChartDetailInfos(pieChartPair.getValue());
			}
			break;
		case JSON:
			try {
				lineChart = buildLineChart(payload);
				model.setJson(lineChart.toString());
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

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.WEB);
		model.setCities(m_appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setOperators(m_appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setCodes(m_patternManager.queryCodes());

		PatternItem first = m_patternManager.queryUrlPatternRules().iterator().next();

		model.setDefaultApi(first.getName() + "|" + first.getPattern());
		model.setPattermItems(m_patternManager.queryUrlPatterns());
		m_normalizePayload.normalize(model, payload);
	}

}
