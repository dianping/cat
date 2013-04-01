package com.dianping.cat.report.page.metric;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.metric.MetricConfig.MetricFlag;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	@Inject(type = ModelService.class, value = "metric")
	private ModelService<MetricReport> m_service;

	@Inject
	private ServerConfigManager m_manager;

	private MetricReport getReport(Payload payload) {
		String group = payload.getGroup();
		group = "TuanGou";
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(group, payload.getPeriod()) //
		      .setProperty("date", date);

		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "metric")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "metric")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);
		MetricReport report = getReport(payload);
		if (report != null) {
			MetricDisplay display = new MetricDisplay(buildTuanGouMetricConfig(), report.getStartTime());

			display.visitMetricReport(report);

			model.setDisplay(display);
			model.setReport(report);
		}
		m_jspViewer.view(ctx, model);
	}

	private MetricConfig buildTuanGouMetricConfig() {
		MetricConfig config = new MetricConfig();
		MetricFlag indexUrl = new MetricFlag("/index", 1, true, false, false);
		MetricFlag detailUrl = new MetricFlag("/detail", 2, true, false, false);
		MetricFlag payUrl = new MetricFlag("/pay", 3, true, false, false);
		MetricFlag orderKey = new MetricFlag("order", 4, false, true, false);
		MetricFlag sumKey = new MetricFlag("payment.pending", 5, false, true, false);
		MetricFlag totalKey = new MetricFlag("payment.success", 5, false, true, false);

		config.put(indexUrl);
		config.put(detailUrl);
		config.put(payUrl);
		config.put(orderKey);
		config.put(sumKey);
		config.put(totalKey);
		return config;
	}

	private void normalize(Model model, Payload payload) {
		model.setIpAddress(payload.getIpAddress());
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.METRIC);
		model.setLongDate(payload.getDate());
		model.setDisplayDomain(payload.getDomain());
		model.setDomain(payload.getDomain());
	}

}
