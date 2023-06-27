package com.dianping.cat.report.page.browser;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.configuration.web.speed.entity.Step;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.browser.display.*;
import com.dianping.cat.report.page.browser.service.*;
import com.site.lookup.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Handler implements PageHandler<Context> {

	@Inject
	private AjaxDataService m_ajaxDataService;

	@Inject
	private AjaxGraphCreator m_graphCreator;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private WebSpeedConfigManager m_webSpeedConfigManager;

	@Inject
	private WebSpeedService m_webSpeedService;

	@Inject
	private JsErrorLogService m_jsErrorLogService;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	protected Map<String, AjaxDataDetail> buildAjaxComparisonInfo(Payload payload) {
		AjaxDataQueryEntity currentEntity = payload.getQueryEntity1();
		AjaxDataQueryEntity comparisonEntity = payload.getQueryEntity2();
		Map<String, AjaxDataDetail> result = new HashMap<String, AjaxDataDetail>();

		if (currentEntity != null) {
			AjaxDataDetail detail = buildComparisonInfo(currentEntity);

			if (detail != null) {
				result.put("当前值", detail);
			}
		}

		if (comparisonEntity != null) {
			AjaxDataDetail detail = buildComparisonInfo(comparisonEntity);

			if (detail != null) {
				result.put("对比值", detail);
			}
		}
		return result;
	}

	private List<AjaxDataDetail> buildAjaxDataDetails(Payload payload) {
		List<AjaxDataDetail> ajaxDetails = new ArrayList<AjaxDataDetail>();

		try {
			ajaxDetails = m_ajaxDataService.buildAjaxDataDetailInfos(payload.getQueryEntity1(), payload.getGroupByField());
			AjaxQueryType type = AjaxQueryType.findByType(payload.getSort());
			Collections.sort(ajaxDetails, new AjaxDataDetailSorter(type));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return ajaxDetails;
	}

	private AjaxDataDisplayInfo buildAjaxDistributeChart(Payload payload) {
		try {
			return m_graphCreator.buildAjaxDistributeChart(payload.getQueryEntity1(), payload.getGroupByField());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AjaxDataDisplayInfo();
	}

	private LineChart buildAjaxLineChart(Payload payload) {
		AjaxDataQueryEntity entity1 = payload.getQueryEntity1();
		AjaxDataQueryEntity entity2 = payload.getQueryEntity2();
		AjaxQueryType type = AjaxQueryType.findByType(payload.getType());
		LineChart lineChart = new LineChart();

		try {
			lineChart = m_graphCreator.buildLineChart(entity1, entity2, type);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineChart;
	}

	private AjaxDataDetail buildComparisonInfo(AjaxDataQueryEntity entity) {
		AjaxDataDetail appDetail = null;

		try {
			List<AjaxDataDetail> appDetails = m_ajaxDataService.buildAjaxDataDetailInfos(entity, AjaxDataField.CODE);

			if (appDetails.size() >= 1) {
				appDetail = appDetails.iterator().next();
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return appDetail;
	}

	private void buildSpeedBarCharts(Payload payload, Model model) {
		try {
			Map<String, Speed> speeds = m_webSpeedConfigManager.getSpeeds();
			SpeedQueryEntity queryEntity1 = normalizeSpeedQueryEntity(payload, speeds);
			WebSpeedDisplayInfo info = m_webSpeedService.buildBarCharts(queryEntity1);

			model.setSpeeds(speeds);
			model.setWebSpeedDisplayInfo(info);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void buildSpeedInfo(Payload payload, Model model) {
		try {
			Map<String, Speed> speeds = m_webSpeedConfigManager.getSpeeds();
			SpeedQueryEntity queryEntity1 = normalizeSpeedQueryEntity(payload, speeds);
			WebSpeedDisplayInfo info = m_webSpeedService.buildSpeedDisplayInfo(queryEntity1,
			      payload.getSpeedQueryEntity2());

			model.setSpeeds(speeds);
			model.setWebSpeedDisplayInfo(info);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void buildSpeedInfoJson(Payload payload, Model model) {
		try {
			Map<String, Speed> speeds = m_webSpeedConfigManager.getSpeeds();
			SpeedQueryEntity queryEntity1 = normalizeSpeedQueryEntity(payload, speeds);
			WebSpeedDisplayInfo info = m_webSpeedService.buildSpeedDisplayInfo(queryEntity1,
			      payload.getSpeedQueryEntity2());
			Map<String, Object> jsonObjs = new HashMap<String, Object>();

			jsonObjs.put("webSpeedDetails", info.getWebSpeedDetails());
			jsonObjs.put("webSpeedSummarys", info.getWebSpeedSummarys());

			model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void fetchConfig(Payload payload, Model model) {
		String type = payload.getType();
		try {
			if ("xml".equalsIgnoreCase(type)) {
				model.setFetchData(m_webSpeedConfigManager.getConfig().toString());
			} else if (StringUtils.isEmpty(type) || "json".equalsIgnoreCase(type)) {
				model.setFetchData(m_jsonBuilder.toJson(m_webSpeedConfigManager.getConfig()));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T fetchTaskResult(List<FutureTask> tasks, int i) {
		T data = null;
		FutureTask task = tasks.get(i);

		try {
			data = (T) task.get(10L, TimeUnit.SECONDS);
		} catch (Exception e) {
			task.cancel(true);
			Cat.logError(e);
		}
		return data;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "browser")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "browser")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		switch (action) {
		case AJAX_LINECHART:
			parallelBuildAjaxLineChart(model, payload);
			break;
		case AJAX_PIECHART:
			AjaxDataDisplayInfo info = buildAjaxDistributeChart(payload);
			model.setAjaxDataDisplayInfo(info);
			break;
		case JS_ERROR:
			viewJsError(payload, model);
			break;
		case JS_ERROR_DETAIL:
			viewJsErrorDetail(payload, model);
			break;
		case SPEED:
			buildSpeedInfo(payload, model);
			break;
		case SPEED_JSON:
			buildSpeedInfoJson(payload, model);
			break;
		case SPEED_GRAPH:
			buildSpeedBarCharts(payload, model);
			break;
		case SPEED_CONFIG_FETCH:
			fetchConfig(payload, model);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.BROWSER);
		model.setCities(m_webConfigManager.queryConfigItem(WebConfigManager.CITY));
		model.setOperators(m_webConfigManager.queryConfigItem(WebConfigManager.OPERATOR));
		model.setNetworks(m_webConfigManager.queryConfigItem(WebConfigManager.NETWORK));
		model.setPlatforms(m_webConfigManager.queryConfigItem(WebConfigManager.PLATFORM));
		model.setSources(m_webConfigManager.queryConfigItem(WebConfigManager.SOURCE));
		model.setCodes(m_patternManager.queryCodes());

		PatternItem first = m_patternManager.queryUrlPatternRules().iterator().next();

		model.setDefaultApi(first.getName() + "|" + first.getPattern());
		model.setPattermItems(m_patternManager.queryUrlPatterns());
		m_normalizePayload.normalize(model, payload);
	}

	private SpeedQueryEntity normalizeSpeedQueryEntity(Payload payload, Map<String, Speed> speeds) {
		SpeedQueryEntity query1 = payload.getSpeedQueryEntity1();

		if (StringUtils.isEmpty(payload.getQuery1())) {
			if (!speeds.isEmpty()) {
				Speed first = speeds.get(speeds.keySet().toArray()[0]);
				Map<Integer, Step> steps = first.getSteps();

				if (first != null && !steps.isEmpty()) {
					String pageId = first.getPage();
					int stepId = steps.get(steps.keySet().toArray()[0]).getId();

					query1.setPageId(pageId);
					query1.setStepId(stepId);

					String split = ";";
					StringBuilder sb = new StringBuilder();

					sb.append(split).append(first.getId()).append("|").append(pageId).append(split).append(stepId)
					      .append(split).append(split).append(split).append(split).append(split);

					payload.setQuery1(sb.toString());
				}
			}
		}

		return query1;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parallelBuildAjaxLineChart(Model model, final Payload payload) {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		List<FutureTask> tasks = new LinkedList<FutureTask>();

		FutureTask lineChartTask = new FutureTask(new Callable<LineChart>() {
			@Override
			public LineChart call() throws Exception {
				return buildAjaxLineChart(payload);
			}
		});

		tasks.add(lineChartTask);
		executor.execute(lineChartTask);

		FutureTask ajaxDetailTask = new FutureTask(new Callable<List<AjaxDataDetail>>() {
			@Override
			public List<AjaxDataDetail> call() throws Exception {
				return buildAjaxDataDetails(payload);
			}

		});
		tasks.add(ajaxDetailTask);
		executor.execute(ajaxDetailTask);

		FutureTask comparisonTask = new FutureTask(new Callable<Map<String, AjaxDataDetail>>() {
			@Override
			public Map<String, AjaxDataDetail> call() throws Exception {
				return buildAjaxComparisonInfo(payload);
			}
		});
		tasks.add(comparisonTask);
		executor.execute(comparisonTask);

		LineChart lineChart = fetchTaskResult(tasks, 0);
		List<AjaxDataDetail> ajaxDataDetails = fetchTaskResult(tasks, 1);
		Map<String, AjaxDataDetail> comparisonDetails = fetchTaskResult(tasks, 2);

		executor.shutdown();

		AjaxDataDisplayInfo info = new AjaxDataDisplayInfo();

		info.setLineChart(lineChart);
		info.setAjaxDataDetailInfos(ajaxDataDetails);
		info.setComparisonAjaxDetails(comparisonDetails);
		model.setAjaxDataDisplayInfo(info);
	}

	private void viewJsError(Payload payload, Model model) {
		JsErrorQueryEntity jsErrorQuery = payload.getJsErrorQuery();
		JsErrorDisplayInfo info = m_jsErrorLogService.buildJsErrorDisplayInfo(jsErrorQuery);
		model.setJsErrorDisplayInfo(info);
	}

	private void viewJsErrorDetail(Payload payload, Model model) {
		JsErrorDetailInfo info = m_jsErrorLogService.queryJsErrorInfo(payload.getId());
		model.setJsErrorDetailInfo(info);
	}

}
