package com.dianping.cat.report.page.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.chart.AggregationGraphCreator;
import com.dianping.cat.report.chart.GraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.network.nettopology.NetGraphManager;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.system.config.MetricAggregationConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricAggregationConfigManager m_metricAggregationConfigManager;

	@Inject
	private AggregationGraphCreator m_aggregationGraphCreator;

	@Inject
	private GraphCreator m_graphCreator;

	@Inject
	private NetGraphManager m_netGraphManager;
	
	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "network")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "network")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		normalize(model, payload);

		long date = payload.getDate();
		int timeRange = payload.getTimeRange();
		Date start = new Date(date - (timeRange - 1) * TimeUtil.ONE_HOUR);
		Date end = new Date(date + TimeUtil.ONE_HOUR);

		switch (payload.getAction()) {
		case NETWORK:
			Map<String, LineChart> charts = m_graphCreator.buildChartsByProductLine(payload.getProduct(), start, end);

			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			break;
		case AGGREGATION:
			Map<String, LineChart> allCharts = m_aggregationGraphCreator.buildDashboardByGroup(start, end,
			      payload.getGroup());
			model.setLineCharts(new ArrayList<LineChart>(allCharts.values()));
			break;
		case NETTOPOLOGY:
			model.setNetGraphData(m_netGraphManager.getNetGraphData(model.getStartTime(), model.getMinute()));
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.NETWORK);

		Map<String, MetricAggregationGroup> metricAggregationGroups = m_metricAggregationConfigManager
		      .getMetricAggregationConfig().getMetricAggregationGroups();
		List<MetricAggregationGroup> metricAggregationGroupList = new ArrayList<MetricAggregationGroup>();

		for (Entry<String, MetricAggregationGroup> entry : metricAggregationGroups.entrySet()) {
			if ("network".equalsIgnoreCase(entry.getValue().getDisplay())) {
				metricAggregationGroupList.add(entry.getValue());
			}
		}
		String poduct = payload.getProduct();

		if (poduct == null || poduct.length() == 0) {

			if ((payload.getGroup() == null || payload.getGroup() == "") && !metricAggregationGroups.isEmpty()) {
				payload.setAction(Action.NETTOPOLOGY.getName());
			} else {
				payload.setAction(Action.AGGREGATION.getName());
			}

		}

		model.setMetricAggregationGroup(metricAggregationGroupList);
		model.setProductLines(m_productLineConfigManager.queryNetworkProductLines().values());

		m_normalizePayload.normalize(model, payload);

		if (payload.getAction().equals(Action.NETTOPOLOGY)) {
			long current = System.currentTimeMillis() - TimeUtil.ONE_MINUTE;
			int curMinute = (int) ((current - current % TimeUtil.ONE_MINUTE) % TimeUtil.ONE_HOUR / TimeUtil.ONE_MINUTE);
			Date start = new Date(payload.getDate());
			Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR - 1);

			int minute = payload.getMinute();
			if (minute == -1) {
				minute = curMinute;
			}

			int maxMinute;
			if (start.getTime() > ModelPeriod.LAST.getStartTime()) {
				maxMinute = curMinute;
			} else {
				maxMinute = 59;
			}

			List<Integer> minutes = new ArrayList<Integer>();
			for (int i = 0; i < 60; i++) {
				minutes.add(i);
			}

			model.setMinutes(minutes);
			model.setMinute(minute);
			model.setMaxMinute(maxMinute);
			model.setStartTime(start);
			model.setEndTime(end);
			model.setIpAddress(payload.getIpAddress());
			model.setAction(payload.getAction());
			model.setDisplayDomain(payload.getDomain());
		} else {
			int timeRange = payload.getTimeRange();
			Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeUtil.ONE_HOUR);
			Date endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1);

			model.setStartTime(startTime);
			model.setEndTime(endTime);
		}
	}

}
