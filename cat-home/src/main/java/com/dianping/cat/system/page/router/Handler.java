package com.dianping.cat.system.page.router;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.KVConfig;
import com.dianping.cat.helper.JsonBuilder;
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

	private String buildRouterInfo(String domain, RouterConfig report) {
		Domain domainConfig = m_configManager.getRouterConfig().findDomain(domain);

		if (domainConfig == null || domainConfig.getServers().isEmpty()) {
			if (report != null) {
				Domain d = report.findDomain(domain);
				String str = null;

				if (d == null) {
					m_configManager.getRouterConfig().getDefaultServers();

					List<Server> servers = m_configManager.queryServersByDomain(domain);

					str = buildServerStr(servers);
				} else {
					List<Server> servers = d.getServers();

					str = buildServerStr(servers);
				}
				return str;
			} else {
				List<Server> servers = m_configManager.queryServersByDomain(domain);

				return buildServerStr(servers);
			}
		} else {
			return buildServerStr(domainConfig.getServers());
		}
	}

	private String buildSampleInfo(String domain) {
		double sample = 1;
		Domain domainConfig = m_configManager.getRouterConfig().findDomain(domain);

		if (domainConfig != null) {
			sample = domainConfig.getSample();
		}
		return String.valueOf(sample);
	}

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
		String domain = payload.getDomain();

		switch (action) {
		case API:
			String routerInfo = buildRouterInfo(domain, report);

			model.setContent(routerInfo);
			break;
		case JSON:
			KVConfig config = new KVConfig();
			Map<String, String> kvs = config.getKvs();

			kvs.put("routers", buildRouterInfo(domain, report));
			kvs.put("sample", buildSampleInfo(domain));
			model.setContent(new JsonBuilder().toJson(config));
			break;
		case MODEL:
			if (report != null) {
				model.setContent(report.toString());
			}
		}

		ctx.getHttpServletResponse().getWriter().write(model.getContent());
	}
}
