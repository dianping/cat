package com.dianping.cat.report.page.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppGraphCreator;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.app.display.Sorter;
import com.dianping.cat.report.page.app.processor.CrashLogProcessor;
import com.dianping.cat.report.service.app.AppDataField;
import com.dianping.cat.report.service.app.AppDataService;
import com.dianping.cat.report.service.app.AppSpeedService;
import com.dianping.cat.report.service.app.CommandQueryEntity;
import com.dianping.cat.report.service.app.SpeedQueryEntity;
import com.dianping.cat.system.config.AppRuleConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppConfigManager m_manager;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppSpeedService m_appSpeedService;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private CrashLogProcessor m_crashLogProcessor;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	private Pair<LineChart, List<AppDataDetail>> buildLineChart(Model model, Payload payload, AppDataField field,
	      String sortBy) {
		CommandQueryEntity entity1 = payload.getQueryEntity1();
		CommandQueryEntity entity2 = payload.getQueryEntity2();
		String type = payload.getType();
		LineChart lineChart = new LineChart();
		List<AppDataDetail> appDetails = new ArrayList<AppDataDetail>();

		try {
			filterCommands(model, payload.isShowActivity());

			lineChart = m_appGraphCreator.buildLineChart(entity1, entity2, type);
			appDetails = m_appDataService.buildAppDataDetailInfos(entity1, field);
			Collections.sort(appDetails, new Sorter(sortBy).buildLineChartInfoComparator());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<LineChart, List<AppDataDetail>>(lineChart, appDetails);

	}

	private Pair<PieChart, List<PieChartDetailInfo>> buildPieChart(Payload payload, AppDataField field) {
		try {
			Pair<PieChart, List<PieChartDetailInfo>> pair = m_appGraphCreator.buildPieChart(payload.getQueryEntity1(),
			      field);
			List<PieChartDetailInfo> infos = pair.getValue();
			Collections.sort(infos, new Sorter().buildPieChartInfoComparator());

			return pair;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	private void filterCommands(Model model, boolean isShowActivity) {
		List<Command> commands = model.getCommands();
		List<Command> remainCommands = new ArrayList<Command>();

		if (isShowActivity) {
			for (Command command : commands) {
				int commandId = command.getId();
				if (commandId >= 1000 && commandId <= 1500) {
					remainCommands.add(command);
				}
			}
		} else {
			for (Command command : commands) {
				int commandId = command.getId();
				if (commandId > 0 && commandId < 1000) {
					remainCommands.add(command);
				}
			}
		}
		model.setCommands(remainCommands);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "app")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "app")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);
		AppDataField field = payload.getGroupByField();
		String sortBy = payload.getSort();

		switch (action) {
		case LINECHART:
			Pair<LineChart, List<AppDataDetail>> lineChartPair = buildLineChart(model, payload, field, sortBy);

			model.setLineChart(lineChartPair.getKey());
			model.setAppDataDetailInfos(lineChartPair.getValue());
			break;
		case PIECHART:
			Pair<PieChart, List<PieChartDetailInfo>> pieChartPair = buildPieChart(payload, field);

			if (pieChartPair != null) {
				model.setPieChart(pieChartPair.getKey());
				model.setPieChartDetailInfos(pieChartPair.getValue());
			}
			int commandId = payload.getQueryEntity1().getId();

			model.setCommandId(commandId);
			model.setCodes(m_manager.queryInternalCodes(commandId));
			break;
		case LINECHART_JSON:
			Pair<LineChart, List<AppDataDetail>> lineChartJsonPair = buildLineChart(model, payload, field, sortBy);

			if (lineChartJsonPair != null) {
				Map<String, Object> lineChartObjs = new HashMap<String, Object>();

				lineChartObjs.put("lineCharts", lineChartJsonPair.getKey());
				lineChartObjs.put("lineChartDetails", lineChartJsonPair.getValue());
				model.setFetchData(new JsonBuilder().toJson(lineChartObjs));
			}
			break;
		case PIECHART_JSON:
			Pair<PieChart, List<PieChartDetailInfo>> pieChartJsonPair = buildPieChart(payload, field);

			if (pieChartJsonPair != null) {
				Map<String, Object> pieChartObjs = new HashMap<String, Object>();

				pieChartObjs.put("pieCharts", pieChartJsonPair.getKey());
				pieChartObjs.put("pieChartDetails", pieChartJsonPair.getValue());
				model.setFetchData(new JsonBuilder().toJson(pieChartObjs));
			}
			break;
		case APP_ADD:
			String domain = payload.getDomain();
			String name = payload.getName();
			String title = payload.getTitle();
			String type = payload.getType();

			if (StringUtils.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				if (m_manager.isNameDuplicate(name)) {
					setUpdateResult(model, 3);
				} else {
					try {
						Pair<Boolean, Integer> addCommandResult = m_manager.addCommand(domain, title, name, type);

						if (addCommandResult.getKey()) {
							setUpdateResult(model, 1);
							m_appRuleConfigManager.addDefultRule(name, addCommandResult.getValue());
						} else {
							setUpdateResult(model, 2);
						}
					} catch (Exception e) {
						setUpdateResult(model, 2);
					}
				}
			}
			break;
		case APP_DELETE:
			domain = payload.getDomain();
			name = payload.getName();

			if (StringUtils.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				Pair<Boolean, List<Integer>> deleteCommandResult = m_manager.deleteCommand(domain, name);
				if (deleteCommandResult.getKey()) {
					setUpdateResult(model, 1);
					m_appRuleConfigManager.deleteDefaultRule(name, deleteCommandResult.getValue());
				} else {
					setUpdateResult(model, 2);
				}
			}
			break;
		case APP_CONFIG_FETCH:
			type = payload.getType();

			try {
				if ("xml".equalsIgnoreCase(type)) {
					model.setFetchData(m_manager.getConfig().toString());
				} else if (StringUtils.isEmpty(type) || "json".equalsIgnoreCase(type)) {
					model.setFetchData(new JsonBuilder().toJson(m_manager.getConfig()));
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case HOURLY_CRASH_LOG:
		case HISTORY_CRASH_LOG:
			try {
				m_crashLogProcessor.process(action, payload, model);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case SPEED:
			try {
				Map<String, List<Speed>> speeds = buildPageStepInfo();
				model.setSpeeds(speeds);

				SpeedQueryEntity queryEntity1 = normalizeQueryEntity(payload, speeds);
				AppSpeedDisplayInfo info = m_appSpeedService.buildSpeedDisplayInfo(queryEntity1,
				      payload.getSpeedQueryEntity2());

				model.setAppSpeedDisplayInfo(info);
			} catch (Exception e) {
				Cat.logError(e);
				e.printStackTrace();
			}
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private SpeedQueryEntity normalizeQueryEntity(Payload payload, Map<String, List<Speed>> speeds) {
		SpeedQueryEntity query1 = payload.getSpeedQueryEntity1();

		if (StringUtils.isEmpty(payload.getQuery1())) {
			if (!speeds.isEmpty()) {
				List<Speed> first = speeds.get(speeds.keySet().toArray()[0]);

				if (first != null && !first.isEmpty()) {
					query1.setId(first.get(0).getId());
				}
			}
		}
		return query1;
	}

	private Map<String, List<Speed>> buildPageStepInfo() {
		Map<String, List<Speed>> page2Steps = new HashMap<String, List<Speed>>();

		for (Speed speed : m_appSpeedConfigManager.getConfig().getSpeeds().values()) {
			String page = speed.getPage();
			if (StringUtils.isEmpty(page)) {
				page = "default";
			}
			List<Speed> steps = page2Steps.get(page);
			if (steps == null) {
				steps = new ArrayList<Speed>();
				page2Steps.put(page, steps);
			}
			steps.add(speed);
		}
		for (Entry<String, List<Speed>> entry : page2Steps.entrySet()) {
			List<Speed> speeds = entry.getValue();
			Collections.sort(speeds, new Comparator<Speed>() {

				@Override
				public int compare(Speed o1, Speed o2) {
					return o1.getStep() - o2.getStep();
				}
			});
		}
		return page2Steps;
	}

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.APP);
		model.setConnectionTypes(m_manager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(m_manager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(m_manager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(m_manager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(m_manager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(m_manager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(m_manager.queryCommands());

		m_normalizePayload.normalize(model, payload);
	}

	private void setUpdateResult(Model model, int i) {
		switch (i) {
		case 0:
			model.setContent("{\"status\":500, \"info\":\"name is required.\"}");
			break;
		case 1:
			model.setContent("{\"status\":200}");
			break;
		case 2:
			model.setContent("{\"status\":500}");
			break;
		case 3:
			model.setContent("{\"status\":500, \"info\":\"name is duplicated.\"}");
			break;
		}
	}
}
