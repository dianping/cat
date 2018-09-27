package com.dianping.cat.report.page.appstats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.appstats.display.DisplayCommands;
import com.dianping.cat.report.page.appstats.service.AppStatisticBuilder;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppStatisticBuilder m_appStatisticBuilder;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "appstats")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "appstats")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		buillAppStatisticInfo(model, payload);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.APPSTATS);
		model.setConstantsItem(m_mobileConfigManager.getConstantItemByCategory(MobileConstants.SOURCE));

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void buillAppStatisticInfo(Model model, Payload payload) throws IOException {
		AppReport report = m_appStatisticBuilder.queryAppReport(payload.getAppId(), payload.getDayDate());
		DisplayCommands displayCommands = m_appStatisticBuilder.buildDisplayCommands(report, payload.getSort());
		Set<String> codeKeys = m_appStatisticBuilder.buildCodeKeys(displayCommands);
		List<String> piechartCodes = payload.getCodes();

		if (piechartCodes.isEmpty()) {
			piechartCodes = new ArrayList<String>(codeKeys);
		}

		Map<String, PieChart> piecharts = m_appStatisticBuilder.buildCodePiecharts(piechartCodes, displayCommands,
		      payload.getTop());

		model.setPiecharts(piecharts);
		model.setDisplayCommands(displayCommands);
		model.setAppReport(report);
		model.setCodeDistributions(codeKeys);
	}
}
