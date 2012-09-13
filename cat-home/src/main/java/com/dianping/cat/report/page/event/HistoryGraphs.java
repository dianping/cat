package com.dianping.cat.report.page.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.GraphEntity;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.page.event.Handler.DetailOrder;
import com.dianping.cat.report.page.event.Handler.SummaryOrder;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;

public class HistoryGraphs {
	public static final long ONE_HOUR = 3600 * 1000L;

	@Inject
	private GraphDao m_graphDao;

	private void appendArray(double[] src, int index, String str, int size) {
		String[] values = str.split(",");
		if (values.length < size) {
			for (int i = 0; i < size; i++) {
				src[index + i] = Double.valueOf(str);
			}
		} else {
			for (int i = 0; i < size; i++) {
				src[index + i] = Double.valueOf(values[i]);
			}
		}
	}

	public Map<String, double[]> buildGraphDatas(Date start, Date end, String type, String name, List<Graph> graphs) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR * 12);
		double[] total_count = new double[size];
		double[] failure_count = new double[size];

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(name)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / ONE_HOUR * 12);
				String summaryContent = graph.getSummaryContent();
				String[] allLines = summaryContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[SummaryOrder.TYPE.ordinal()].equals(type)) {
						appendArray(total_count, indexOfperiod, records[SummaryOrder.TOTAL_COUNT.ordinal()], 12);
						appendArray(failure_count, indexOfperiod, records[SummaryOrder.FAILURE_COUNT.ordinal()], 12);
					}
				}
			}
		} else if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(name)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / ONE_HOUR * 12);
				String detailContent = graph.getDetailContent();
				String[] allLines = detailContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[DetailOrder.TYPE.ordinal()].equals(type) && records[DetailOrder.NAME.ordinal()].equals(name)) {

						appendArray(total_count, indexOfperiod, records[DetailOrder.TOTAL_COUNT.ordinal()], 12);
						appendArray(failure_count, indexOfperiod, records[DetailOrder.FAILURE_COUNT.ordinal()], 12);
					}
				}
			}
		}

		result.put("total_count", total_count);
		result.put("failure_count", failure_count);
		return result;
	}

	public void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String type = payload.getType();
		String name = payload.getName();
		String display = name != null ? name : type;

		int size = (int) ((end.getTime() - start.getTime()) / ONE_HOUR * 12);

		HistoryGraphItem item = new HistoryGraphItem();
		item.setStart(start);
		item.setSize(size);

		Map<String, double[]> graphData = getGraphDatas(model, payload);
		double[] failureCount = graphData.get("failure_count");
		double[] totalCount = graphData.get("total_count");

		item.setTitles(display + " Hit Trend");
		item.addValue(totalCount);
		model.setHitTrend(item.getJsonString());

		item.getValues().clear();
		item.setTitles(display + " Failure Trend");
		item.addValue(failureCount);
		model.setFailureTrend(item.getJsonString());
	}

	public Map<String, double[]> getGraphDatas(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ip = model.getIpAddress();
		String queryIP = "All".equals(ip) == true ? "all" : ip;
		List<Graph> events = new ArrayList<Graph>();
		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + ONE_HOUR) {
			try {
				Graph graph = m_graphDao.findSingalByDomainNameIpDuration(new Date(startLong), queryIP, domain, "event",
				      GraphEntity.READSET_FULL);
				events.add(graph);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		// try {
		// events = this.m_graphDao.findByDomainNameIpDuration(start, end, queryIP, domain, "event",
		// GraphEntity.READSET_FULL);
		// } catch (Exception e) {
		// Cat.logError(e);
		// }
		Map<String, double[]> result = buildGraphDatas(start, end, type, name, events);
		return result;
	}
}
