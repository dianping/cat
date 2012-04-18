package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.view.StringSortHelper;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject
	private GraphBuilder m_builder;

	@Inject(type = ModelService.class, value = "heartbeat")
	private ModelService<HeartbeatReport> m_service;

	private String getIpAddress(HeartbeatReport report, Payload payload) {
		Set<String> ips = report.getIps();
		String ip = payload.getIpAddress();

		if ((ip == null || ip.length() == 0) && !ips.isEmpty()) {
			ip = StringSortHelper.sort(ips).get(0);
		}

		return ip;
	}

	private HeartbeatReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "h")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "h")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.HEARTBEAT);
		model.setIpAddress(payload.getIpAddress());

		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void showReport(Model model, Payload payload) {
		try {
			ModelPeriod period = payload.getPeriod();

			if (period.isFuture()) {
				model.setLongDate(payload.getCurrentDate());
			} else {
				model.setLongDate(payload.getDate());
			}
			model.setDisplayDomain(payload.getDomain());

			HeartbeatReport report = getReport(payload);
			if (report == null) {
				return;
			}
			model.setReport(report);
			String ip = getIpAddress(report, payload);
			model.setIpAddress(ip);

			DisplayHeartbeat displayHeartbeat = new DisplayHeartbeat(m_builder);
			model.setResult(displayHeartbeat.display(report, ip));
			model.setActiveThreadGraph(displayHeartbeat.getActiceThreadGraph());
			model.setDaemonThreadGraph(displayHeartbeat.getDeamonThreadGraph());
			model.setTotalThreadGraph(displayHeartbeat.getTotalThreadGraph());
			model.setStartedThreadGraph(displayHeartbeat.getStartedThreadGraph());
		} catch (Throwable e) {
			Cat.getProducer().logError(e);
			model.setException(e);
		}
	}
}
