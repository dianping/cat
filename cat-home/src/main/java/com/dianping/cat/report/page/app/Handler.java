package com.dianping.cat.report.page.app;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.app.graph.AppGraphCreator;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppConfigManager m_manager;

	@Inject
	private AppGraphCreator m_appGraphCreator;

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

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.APP);
		model.setConnectionTypes(m_manager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(m_manager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(m_manager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(m_manager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(m_manager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(m_manager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(m_manager.queryCommands());
		Payload payload = ctx.getPayload();

		QueryEntity entity1 = payload.getQueryEntity1();
		QueryEntity entity2 = payload.getQueryEntity2();
		String type = payload.getType();

		if (StringUtils.isEmpty(type)) {
			type = "successRatio";
		}

		LineChart lineCharts = new LineChart();

		if (entity1 != null) {
			LineChart lineChart1 = m_appGraphCreator.buildLineChart(entity1, type);
			Iterator<Map<Long, Double>> idata = lineChart1.getDatas().iterator();

			if (lineChart1.getDatas().size() == 1) {
				lineCharts.add("查询1", idata.next());
			}
		}

		if (entity2 != null) {
			LineChart lineChart2 = m_appGraphCreator.buildLineChart(entity2, type);
			Iterator<Map<Long, Double>> idata = lineChart2.getDatas().iterator();

			if (lineChart2.getDatas().size() == 1) {
				lineCharts.add("查询2", idata.next());
			}
		}

		lineCharts.setId("app");
		lineCharts.setHtmlTitle(type);
		model.setLineChart(lineCharts);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
