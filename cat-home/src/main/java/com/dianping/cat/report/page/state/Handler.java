package com.dianping.cat.report.page.state;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject
	private ServerConfigManager m_manager;

	@Inject(type = ModelService.class, value = "state")
	private ModelService<StateReport> m_service;

	private StateReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date)//
		      .setProperty("ip", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<StateReport> response = m_service.invoke(request);
			StateReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable sql service registered for " + request + "!");
		}

	}

	public StateReport getHistoryReport(Payload payload) {
		String domain = "Cat";
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		return m_reportService.queryStateReport(domain, start, end);
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "state")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "state")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);
		StateReport report = null;
		switch (action) {
		case HOURLY:
			report = getHourlyReport(payload);
			break;
		case HISTORY:
			report = getHistoryReport(payload);
			break;
		}

		StateShow show = new StateShow(payload.getIpAddress());
		show.visitStateReport(report);
		model.setState(show);
		model.setReport(report);

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.STATE);
		model.setDisplayDomain(payload.getDomain());
		// Only for cat
		payload.setDomain(m_manager.getConsoleDefaultDomain());
		String ip = payload.getIpAddress();

		if (ip == null || ip.length() == 0) {
			payload.setIpAddress(CatString.ALL_IP);
		}
		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
		model.setIpAddress(payload.getIpAddress());

		if (action == Action.HISTORY) {
			String type = payload.getReportType();
			if (type == null || type.length() == 0) {
				payload.setReportType("day");
			}
			model.setReportType(payload.getReportType());
			payload.computeStartDate();
			if (!payload.isToday()) {
				payload.setYesterdayDefault();
			}
			model.setLongDate(payload.getDate());
			model.setCustomDate(payload.getHistoryStartDate(), payload.getHistoryEndDate());
		}
	}
}
