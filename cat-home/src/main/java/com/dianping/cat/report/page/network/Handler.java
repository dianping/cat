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

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.chart.AggregationGraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.system.config.MetricAggregationConfigManager;
import com.dianping.cat.system.config.MetricGroupConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricGroupConfigManager m_metricGroupConfigManager;

	@Inject
	private MetricAggregationConfigManager m_metricAggregationConfigManager;

	@Inject
	private AggregationGraphCreator m_aggregationGraphCreator;

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
		
//		Map<String, ProductLine> productLines = m_productLineConfigManager.queryNetworkProductLines();
		Map<String, MetricAggregationGroup> metricAggregationGroups = m_metricAggregationConfigManager
		      .getMetricAggregationConfig().getMetricAggregationGroups();
//		List<MetricAggregationGroup> metricAggregationGroupList = new ArrayList<MetricAggregationGroup>();
//		
//		for (Entry<String, MetricAggregationGroup> entry : metricAggregationGroups.entrySet()) {
//	      if(productLines.containsKey(entry.getKey())) {
//	      	metricAggregationGroupList.add(entry.getValue());
//	      }
//      }
		
//		if (payload.getGroup() == null) {
//			if (!metricAggregationGroupList.isEmpty()) {
//				String metricAggregationGroup = ((MetricAggregationGroup) metricAggregationGroupList.get(0)).getId();
//
//				payload.setGroup(metricAggregationGroup);
//			}
//		}
//		System.out.println(metricAggregationGroupList);
		
		switch (payload.getAction()) {
		case NETWORK:
			Map<String, LineChart> charts = m_aggregationGraphCreator
			      .buildDashboardByGroup(start, end, payload.getGroup());
			
			model.setLineCharts(new ArrayList<LineChart>(charts.values()));
			model.setMetricAggregationGroup(metricAggregationGroups.values());
			break;
		default:
			throw new RuntimeException("Unknown action: " + payload.getAction());
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Model model, Payload payload) {
		model.setPage(ReportPage.NETWORK);
		String poduct = payload.getProduct();

		if (poduct == null || poduct.length() == 0) {
			payload.setAction(Action.NETWORK.getName());
		}
		m_normalizePayload.normalize(model, payload);
		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeUtil.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);
	}
}
