package com.dianping.cat.report.page.matrix;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {

	@Inject
	private MatrixReportService m_reportService;

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject(type = ModelService.class, value = MatrixAnalyzer.ID)
	private ModelService<MatrixReport> m_service;

	private MatrixReport getHourlyReport(Payload payload) {
		String domain = payload.getDomain();
		String ipAddress = payload.getIpAddress();
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
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
	@InboundActionMeta(name = MatrixAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = MatrixAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
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

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.MATRIX);
		m_normalizePayload.normalize(model, payload);
	}

	private void showSummarizeReport(Model model, Payload payload) {
		String domain = payload.getDomain();

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		MatrixReport matrixReport = m_reportService.queryReport(domain, start, end);

		if (matrixReport == null) {
			return;
		}
		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		model.setReport(matrixReport);
		model.setMatrix(new DisplayMatrix(matrixReport).setSortBy(payload.getSortBy()));
	}

}
