package com.dianping.cat.report.page.matrix;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {

	@Inject
	private ReportService m_reportService;
	
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ServerConfigManager m_manager;

	@Inject(type = ModelService.class, value = "matrix")
	private ModelService<MatrixReport> m_service;

	private MatrixReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date) //
		      .setProperty("ip", ipAddress);

		if (m_service.isEligable(request)) {
			ModelResponse<MatrixReport> response = m_service.invoke(request);
			MatrixReport report = response.getModel();
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable matrix service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "matrix")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "matrix")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		normalize(model, payload);
		switch (payload.getAction()) {
		case HISTORY_REPORT:
			showSummarizeReport(model, payload);
			break;
		case HOURLY_REPORT:
			MatrixReport report = getHourlyReport(payload);
			model.setReport(report);
			model.setMatrix(new DisplayMatrix(report).setSortBy(payload.getSortBy()));
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void showSummarizeReport(Model model, Payload payload) {
		String domain = payload.getDomain();

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		MatrixReport matrixReport = m_reportService.queryMatrixReport(domain, start, end);
		
		if (matrixReport == null) {
			return;
		}
		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		model.setReport(matrixReport);
		model.setMatrix(new DisplayMatrix(matrixReport).setSortBy(payload.getSortBy()));
	}

	public void normalize(Model model, Payload payload) {
		Action action = payload.getAction();
		model.setAction(action);
		model.setPage(ReportPage.MATRIX);

		if (StringUtils.isEmpty(payload.getDomain())) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}

		model.setDisplayDomain(payload.getDomain());

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}
		if (action == Action.HISTORY_REPORT) {
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
