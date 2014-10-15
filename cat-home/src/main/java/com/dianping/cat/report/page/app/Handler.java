package com.dianping.cat.report.page.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataGroupByField;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.AppDataSpreadInfo;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.app.graph.AppGraphCreator;
import com.dianping.cat.system.config.AppRuleConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppConfigManager m_manager;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

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
				if (commandId >= 0 && commandId <= 200) {
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
		AppDataGroupByField field = payload.getGroupByField();
		String sortBy = payload.getSort();

		switch (action) {
		case VIEW:
			QueryEntity linechartEntity1 = payload.getQueryEntity1();
			QueryEntity linechartEntity2 = payload.getQueryEntity2();
			String type = payload.getType();
			try {
				LineChart lineChart = m_appGraphCreator.buildLineChart(linechartEntity1, linechartEntity2, type);
				List<AppDataSpreadInfo> appDataSpreadInfos = m_appDataService.buildAppDataSpreadInfo(linechartEntity1,
				      field);
				Collections.sort(appDataSpreadInfos, new Sorter(sortBy).buildLineChartInfoComparator());

				model.setLineChart(lineChart);
				model.setAppDataSpreadInfos(appDataSpreadInfos);
				filterCommands(model, payload.isShowActivity());
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case PIECHART:
			try {
				Pair<PieChart, List<PieChartDetailInfo>> pair = m_appGraphCreator.buildPieChart(payload.getQueryEntity1(),
				      field);
				List<PieChartDetailInfo> infos = pair.getValue();
				Collections.sort(infos, new Sorter(sortBy).buildPieChartInfoComparator());

				model.setPieChart(pair.getKey());
				model.setPieChartDetailInfos(infos);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_ADD:
			String domain = payload.getDomain();
			String name = payload.getName();
			String title = payload.getTitle();

			if (StringUtil.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				try {
					Pair<Boolean, Integer> addCommandResult = m_manager.addCommand(domain, title, name);

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
			break;
		case APP_DELETE:
			domain = payload.getDomain();
			name = payload.getName();

			if (StringUtil.isEmpty(name)) {
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
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Model model, Payload payload) {
		model.setAction(Action.VIEW);
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
		}
	}
}
