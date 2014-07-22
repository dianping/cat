package com.dianping.cat.report.page.app;

import java.io.IOException;
import javax.servlet.ServletException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.app.graph.AppGraphCreator;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppConfigManager m_manager;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private PayloadNormalizer m_normalizePayload;

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
		
		QueryEntity entity1 = payload.getQueryEntity1();
		QueryEntity entity2 = payload.getQueryEntity2();
		String type = payload.getType();
		LineChart lineChart = m_appGraphCreator.buildLineChart(entity1, entity2, type);

		model.setLineChart(lineChart);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
