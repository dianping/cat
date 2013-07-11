package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.system.page.abtest.service.ABTestService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "metric")
	private ModelService<MetricReport> m_service;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private MetricConfigManager m_configManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private ABTestService m_abtestService;

	private static final String TUAN = "TuanGou";

	private MetricReport getReport(Payload payload) {
		String product = payload.getProduct();
		ModelRequest request = new ModelRequest(product, payload.getDate());
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
		String test = payload.getTest();

		if (report != null) {
			Date startTime = report.getStartTime();
			if (startTime == null) {
				startTime = payload.getHistoryStartDate();
			}
			String product = payload.getProduct();
			List<String> domains = m_productLineConfigManager.queryProductLineDomains(product);
			List<MetricItemConfig> configs = m_configManager.queryMetricItemConfigs(new HashSet<String>(domains));
			MetricDisplay display = new MetricDisplay(configs, test, startTime);

			display.setAbtest(m_abtestService);

			display.visitMetricReport(report);
			model.setDisplay(display);
			model.setReport(report);
			model.setProductLines(m_productLineConfigManager.queryProductLines().values());
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
