package com.dianping.cat.report.page.applog;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.applog.display.AppLogDetailInfo;
import com.dianping.cat.report.page.applog.display.AppLogDisplayInfo;
import com.dianping.cat.report.page.applog.service.AppLogQueryEntity;
import com.dianping.cat.report.page.applog.service.AppLogService;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppLogService m_appLogService;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "applog")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "applog")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		model.setAction(action);
		model.setPage(ReportPage.APPLOG);

		switch (action) {
		case APP_LOG:
			AppLogDisplayInfo displayInfo = buildAppLog(payload);
			model.setAppLogDisplayInfo(displayInfo);
			break;
		case APP_LOG_DETAIL:
			AppLogDetailInfo detailInfo = buildAppLogDetailInfo(payload);
			model.setAppLogDetailInfo(detailInfo);
			break;
		case APP_LOG_GRAPH:
			AppLogDisplayInfo graphInfo = buildAppLogGraph(payload.getAppLogQuery());
			model.setAppLogDisplayInfo(graphInfo);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private AppLogDisplayInfo buildAppLogGraph(AppLogQueryEntity appLogQuery) {
		return m_appLogService.buildAppLogGraph(appLogQuery);
	}

	private AppLogDetailInfo buildAppLogDetailInfo(Payload payload) {
		return m_appLogService.buildAppLogDetail(payload.getId());
	}

	private AppLogDisplayInfo buildAppLog(Payload payload) {
		AppLogQueryEntity entity = payload.getAppLogQuery();
		AppLogDisplayInfo info = m_appLogService.buildAppLogDisplayInfo(entity);

		return info;
	}
}
