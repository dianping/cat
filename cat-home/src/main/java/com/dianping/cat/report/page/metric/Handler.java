package com.dianping.cat.report.page.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private GraphCreator m_graphCreator;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	private Map<String, LineChart> buildLineCharts(Payload payload, Date start, Date end) {
		Map<String, LineChart> allCharts = null;
		String productLine = payload.getProduct();

		if (StringUtils.isEmpty(productLine)) {
			String tag = payload.getTag();

			if (StringUtils.isEmpty(tag)) {
				tag = MetricConfigManager.DEFAULT_TAG;
				payload.setTag(tag);
			}
			allCharts = m_graphCreator.buildDashboardByTag(start, end, tag);
		} else {
			allCharts = m_graphCreator.buildChartsByProductLine(productLine, start, end);
		}
		return allCharts;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = MetricAnalyzer.ID)
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = MetricAnalyzer.ID)
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		long date = payload.getDate();
		int timeRange = payload.getTimeRange();
		Date start = new Date(date - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date end = new Date(date + TimeHelper.ONE_HOUR);
		Map<String, LineChart> allCharts = buildLineCharts(payload, start, end);

		switch (payload.getAction()) {
		case METRIC:
			model.setLineCharts(new ArrayList<LineChart>(allCharts.values()));
			break;
		case JSON:
			String id = payload.getId();
			LineChart lineChart = allCharts.get(id);

			if (lineChart != null) {
				model.setJson(lineChart.getJsonString());
			}
			break;
		}
		List<String> tags = m_metricConfigManager.queryTags();

		model.setTags(tags);
		model.setProductLines(m_productLineConfigManager.queryMetricProductLines().values());
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.METRIC);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);

		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);
	}
}
