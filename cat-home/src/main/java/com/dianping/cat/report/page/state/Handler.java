package com.dianping.cat.report.page.state;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.google.gson.Gson;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private StateGraphs m_stateGraphs;

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	private static final String CAT = "Cat";

	public StateReport getHistoryReport(Payload payload) {
		String domain = CAT;
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryStateReport(domain, start, end);
	}

	private StateReport getHourlyReport(Payload payload) {
		// only for cat
		String domain = CAT;
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
		      .setProperty("ip", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StateReport> response = m_service.invoke(request);
			StateReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable sql service registered for " + request + "!");
		}

	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = StateAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = StateAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);
		String key = payload.getKey();
		StateReport report = null;
		LineChart item = null;
		switch (action) {
		case HOURLY:
			report = getHourlyReport(payload);
			break;
		case HISTORY:
			report = getHistoryReport(payload);
			break;
		case GRAPH:
			report = getHourlyReport(payload);
			item = m_stateGraphs.buildGraph(report, payload.getDomain(), payload.getHistoryStartDate(),
			      payload.getHistoryEndDate(), "graph", key, payload.getIpAddress());
			break;
		case HISTORY_GRAPH:
			item = m_stateGraphs.buildGraph(null, payload.getDomain(), payload.getHistoryStartDate(),
			      payload.getHistoryEndDate(), "historyGraph", key, payload.getIpAddress());
			break;
		}

		if (action == Action.HOURLY || action == Action.HISTORY) {
			StateShow show = new StateShow(payload.getIpAddress());
			show.visitStateReport(report);
			model.setState(show);
			model.setReport(report);
		} else {
			Gson gson = new Gson();
			model.setGraph(gson.toJson(item));
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.STATE);
		String ip = payload.getIpAddress();
		Action action = payload.getAction();
		
		if (action == Action.HOURLY || action == Action.HISTORY) {
			if (!CAT.equalsIgnoreCase(payload.getDomain()) || StringUtils.isEmpty(ip)) {
				payload.setIpAddress(Constants.ALL);
			}
		}
		m_normalizePayload.normalize(model, payload);
	}

}
