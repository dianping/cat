package com.dianping.cat.report.page.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.net.Networks;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.home.nettopo.entity.NetGraph;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.nettopo.entity.NetTopology;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.chart.AggregationGraphCreator;
import com.dianping.cat.report.chart.GraphCreator;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PayloadNormalizer;
import com.dianping.cat.report.page.network.nettopology.NetGraphManager;
import com.dianping.cat.report.service.ReportService;
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

	@Inject
	private ReportService m_reportService;

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
			long curDate = System.currentTimeMillis() / 3600000 * 3600000;
			if (model.getStartTime().equals(new Date(curDate))) {
				model.setNetGraphData(m_netGraphManager.getNetGraphData(model.getMinute()));
			} else {
				model.setMaxMinute(59);
				model.setNetGraphData(queryHistoryNetTopologyData(model));
			}
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private Object querySet(Date date){
		
		if(date.getTime()>ModelPeriod.LAST.getStartTime()){
			//query from netManger;
		}
		else{
			//query from db;
		}
		return null;
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
			payload.setReportType("hour");
			int minute = payload.getMinute();
			Date startTime = null, endTime = null;
			
			long current = System.currentTimeMillis();
			long currentTime = current - current % TimeUtil.ONE_MINUTE - TimeUtil.ONE_MINUTE;
			int curMinute = currentTime ;
			
			Date start =new Date(current - TimeUtil.ONE_MINUTE*5);
			Date end = new Date(start.getTime()+TimeUtil.ONE_HOUR-1);
			
			
			model.setStartTime(cal.getTime());
			
			int curMinute = (int) (current / 60000 % 60) - 1;
			if (minute == -1) {
				if (curMinute == -1) {
					curMinute = 59;
					startTime = new Date(payload.getDate() - TimeUtil.ONE_HOUR);
					endTime = new Date(payload.getDate() - 1);
				}
				minute = curMinute;
			}
			if (startTime == null) {
				startTime = new Date(payload.getDate());
				endTime = new Date(payload.getDate() + TimeUtil.ONE_HOUR - 1);
			}
			List<Integer> minutes = new ArrayList<Integer>();
			for (int i = 0; i < 60; i++) {
				minutes.add(i);
			}
			model.setMinutes(minutes);
			model.setMinute(minute);
			model.setMaxMinute(curMinute);
			model.setStartTime(startTime);
			model.setEndTime(endTime);
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

	private ArrayList<Pair<String, String>> queryHistoryNetTopologyData(Model model) {
		String domain = "Cat";
		Date startTime = model.getStartTime();
		int minute = model.getMinute();
		JsonBuilder jb = new JsonBuilder();
		ArrayList<Pair<String, String>> netGraphData = new ArrayList<Pair<String, String>>();
		NetGraphSet netGraphSet = m_reportService.queryNetTopologyReport(domain, startTime, null);

		if (netGraphSet != null) {
			NetGraph netGraph = netGraphSet.getNetGraphs().get(minute);

			if (netGraph != null) {
				for (NetTopology netTopology : netGraph.getNetTopologies()) {
					String topoName = netTopology.getName();
					String data = jb.toJson(netTopology);
					netGraphData.add(new Pair<String, String>(topoName, data));
				}
			}
		}

		return netGraphData;
	}

}
