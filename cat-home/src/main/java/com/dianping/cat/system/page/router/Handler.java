package com.dianping.cat.system.page.router;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.router.entity.Domain;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.entity.Server;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.RouterConfigService;

public class Handler implements PageHandler<Context> {

	@Inject
	private RouterConfigService m_reportService;

	@Inject
	private RouterConfigManager m_configManager;

	private String buildServerStr(List<Server> servers) {
		StringBuilder sb = new StringBuilder();

		for (Server server : servers) {
			sb.append(server.getId()).append(":").append(server.getPort()).append(";");
		}
		return sb.toString();
	}

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
		Date start = payload.getDate();
		Date end = new Date(start.getTime() + TimeHelper.ONE_DAY);
		RouterConfig report = m_reportService.queryReport(Constants.CAT, start, end);

		switch (action) {
		case API:
			Domain domainConfig = m_configManager.getRouterConfig().findDomain(payload.getDomain());

			if (domainConfig == null) {
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
					List<Server> servers = m_configManager.queryServersByDomain(payload.getDomain());

					model.setContent(buildServerStr(servers));
				}
			} else {
				model.setContent(buildServerStr(domainConfig.getServers()));
			}
			break;
		case MODEL:
			if (report != null) {
				model.setContent(report.toString());
			}
		}

		ctx.getHttpServletResponse().getWriter().write(model.getContent());
	}
}
