package com.dianping.cat.system.page.router;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.config.RouterConfigManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private RouterConfigManager m_configManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "router")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "router")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		Date date = payload.getDate();
		Date end = new Date(date.getTime() + TimeUtil.ONE_DAY);
		RouterConfig report = m_reportService.queryRouterConfigReport(Constants.CAT, date, end);

		switch (action) {
		case API:
			if (report != null) {
				Domain domain = report.findDomain(payload.getDomain());
				String str = null;

				if (domain == null) {
					m_configManager.getRouterConfig().getDefaultServers();

					List<Server> servers = m_configManager.queryServersByDomain(payload.getDomain());

					str = buildServerStr(servers);
				} else {
					List<Server> servers = domain.getServers();

					str = buildServerStr(servers);
				}
				model.setContent(str);
			} else {
				model.setContent("");
			}
			break;
		case MODEL:
			if (report != null) {
				model.setContent(report.toString());
			}
			break;
		}
		model.setAction(Action.API);
		model.setPage(SystemPage.ROUTER);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private String buildServerStr(List<Server> servers) {
		StringBuilder sb = new StringBuilder();

		for (Server server : servers) {
			sb.append(server.getId()).append(":").append(server.getPort()).append(";");
		}
		return sb.toString();
	}
}
