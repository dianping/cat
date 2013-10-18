package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.metric.chart.GraphCreator;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private GraphCreator m_graphCreator;

	private static final String DEFULT_PRODUCT = "TuanGou";

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
		long date = payload.getDate();
		int timeRange = payload.getTimeRange();
		Date start = new Date(date - (timeRange - 1) * TimeUtil.ONE_HOUR);
		Date end = new Date(date + TimeUtil.ONE_HOUR);

		switch (action) {
		case METRIC:
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(payload.getProduct(), start, end,
			      payload.getTest());

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			break;
		case DASHBOARD:
			Map<String, LineChart> allCharts = m_graphCreator.buildDashboard(start, end, payload.getTest());
			
			model.setLineCharts(new ArrayList<LineChart>(allCharts.values()));
			break;
		}
		model.setProductLines(m_productLineConfigManager.queryProductLines().values());
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.METRIC);
		m_normalizePayload.normalize(model, payload);

		String poduct = payload.getProduct();
		if (poduct == null || poduct.length() == 0) {
			payload.setProduct(DEFULT_PRODUCT);
		}
		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeUtil.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);
	}

}
