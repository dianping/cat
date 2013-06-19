package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.advanced.BussinessConfigManager;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "metric")
	private ModelService<MetricReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private BussinessConfigManager m_configManager;
	
	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	private static final String TUAN = "TuanGou";

	private MetricReport getReport(Payload payload) {
		String product = payload.getProduct();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(product, payload.getPeriod()) //
		      .setProperty("date", date);
		if (m_service.isEligable(request)) {
			ModelResponse<MetricReport> response = m_service.invoke(request);
			MetricReport report = response.getModel();

			
			System.out.println(report);
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
		int test = payload.getTest();

		if (report != null) {
			Date startTime = report.getStartTime();
			if (startTime == null) {
				startTime = payload.getHistoryStartDate();
			}
			MetricDisplay display = new MetricDisplay(m_configManager.getConfigs(payload.getProduct()), test, startTime);

			display.visitMetricReport(report);
			model.setDisplay(display);
			model.setReport(report);
			model.setProducts(m_productLineConfigManager.queryProductLines().keySet());
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.METRIC);
		m_normalizePayload.normalize(model, payload);

		String poduct = payload.getProduct();
		if (poduct == null || poduct.length() == 0) {
			payload.setProduct(TUAN);
		}
	}

}
