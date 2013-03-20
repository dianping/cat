package com.dianping.cat.report.page.top;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject(type = ModelService.class, value = "top")
	private ModelService<TopReport> m_service;

	@Inject
	private ServerConfigManager m_manager;

	private TopReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<TopReport> response = m_service.invoke(request);
			TopReport report = response.getModel();

			if (report == null || report.getDomains().size() == 0) {
				report = m_reportService.queryTopReport(domain, new Date(payload.getDate()), new Date(payload.getDate()
				      + TimeUtil.ONE_HOUR));
			}
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "top")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "top")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		TopReport report = getReport(payload);
		ModelPeriod period = payload.getPeriod();
		int count = payload.getCount();
		Metric metrix = new Metric();

		if (!period.isCurrent()) {
			metrix = new Metric(60);
		} else {
			model.setRefresh(true);
		}
		if (count > 0) {
			metrix = new Metric(count);
		}

		metrix.visitTopReport(report);
		model.setTopReport(report);
		model.setMetrix(metrix);
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		payload.setDomain(m_manager.getConsoleDefaultDomain());
		model.setIpAddress(payload.getIpAddress());
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TOP);
		model.setLongDate(payload.getDate());
		model.setDisplayDomain(payload.getDomain());
	}

}
