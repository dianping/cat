package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;

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
	private ReportService m_reportService;

	@Inject
	private BaselineService m_baselineService;

	private static final String TUAN = "TuanGou";

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
		Action action = payload.getAction();

		normalize(model, payload);
		MetricDisplay metricDisplay = null;

		Collection<ProductLine> productLines = m_productLineConfigManager.queryProductLines().values();
		switch (action) {
		case METRIC:
			metricDisplay = buildProductLineMetrics(payload.getProduct(), payload, false);

			model.setLineCharts(metricDisplay.getLineCharts());
			model.setAbtests(metricDisplay.getAbtests());
			break;
		case DASHBOARD:
			List<LineChart> allCharts = new ArrayList<LineChart>();
			for (ProductLine productLine : productLines) {
				metricDisplay = buildProductLineMetrics(productLine.getId(), payload, true);

				List<LineChart> charts = metricDisplay.getLineCharts();

				allCharts.addAll(charts);
			}
			model.setLineCharts(allCharts);
			break;
		}
		model.setProductLines(productLines);
		m_jspViewer.view(ctx, model);
	}

	private MetricDisplay buildProductLineMetrics(String product,
			Payload payload, boolean isDashboard) {
		long date = payload.getDate();
		String abtestID = payload.getTest();
		int timeRange = payload.getTimeRange();
		Date startTime = new Date(date - (timeRange - 1) * TimeUtil.ONE_HOUR);
		List<String> domains = m_productLineConfigManager
				.queryProductLineDomains(product);
		List<MetricItemConfig> metricConfigs = m_configManager
				.queryMetricItemConfigs(new HashSet<String>(domains));
		MetricDisplay display = new MetricDisplay(product, abtestID, startTime,
				isDashboard, timeRange);

		display.initializeLineCharts(metricConfigs);

		display.setBaselineService(m_baselineService).setReportService(m_reportService).setService(m_service);

		display.generateAllCharts();
		
		return display;
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.METRIC);
		m_normalizePayload.normalize(model, payload);

		String poduct = payload.getProduct();
		if (poduct == null || poduct.length() == 0) {
			payload.setProduct(TUAN);
		}
		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeUtil.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);
	}

}
