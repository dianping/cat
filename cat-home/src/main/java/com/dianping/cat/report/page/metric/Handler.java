package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
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

	@Inject
	private ReportService m_reportService;

	@Inject
	private BaselineService m_baselineService;

	private static final String TUAN = "TuanGou";

	private final Map<String, MetricReport> m_metricReportMap = new LinkedHashMap<String, MetricReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, MetricReport> eldest) {
			return size() > 1000;
		}
	};

	private MetricReport getReportFromDB(String product, long date) {
		String key = product + date;
		MetricReport result = m_metricReportMap.get(key);
		if (result == null) {
			Date start = new Date(date);
			Date end = new Date(date + TimeUtil.ONE_HOUR);
			try {
				result = m_reportService.queryMetricReport(product, start, end);
				m_metricReportMap.put(key, result);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return result;
	}

	private MetricReport getReport(ModelPeriod period, String product, long date) {
		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(product, date);
			if (m_service.isEligable(request)) {
				ModelResponse<MetricReport> response = m_service.invoke(request);
				MetricReport report = response.getModel();
				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable metric service registered for " + request + "!");
			}
		} else {
			return getReportFromDB(product, date);
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

		String test = payload.getTest();
		Date startTime = new Date(payload.getDate() - 23 * TimeUtil.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR);
		String product = payload.getProduct();
		List<String> domains = m_productLineConfigManager.queryProductLineDomains(product);
		List<MetricItemConfig> domainSet = m_configManager.queryMetricItemConfigs(new HashSet<String>(domains));
		MetricDisplay display = new MetricDisplay(domainSet, test, startTime);
		display.setBaselineService(m_baselineService);
		display.setAbtest(m_abtestService);

		long date = startTime.getTime();
		MetricReport report;
		for (int i = 0; i < 24; i++) {
			// current hourly and last hourly report read this function
			// last two hours ago read db (cache)
			ModelPeriod period = payload.getPeriod();
			report = getReport(period, product, date);

			if (report != null) {
				display.visitMetricReport(i, report);
			}
			date = date + TimeUtil.ONE_HOUR;
		}
		display.generateDailyLineCharts();
		System.out.println(test  + "test");
		if (test.equals("-1")) {
			display.generateBaselineChart();
		}
		report = new MetricReport();
		report.setStartTime(startTime);
		report.setEndTime(endTime);

		model.setDisplay(display);
		model.setReport(report);
		model.setProductLines(m_productLineConfigManager.queryProductLines().values());
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
