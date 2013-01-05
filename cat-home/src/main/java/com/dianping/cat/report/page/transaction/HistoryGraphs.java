package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.home.dal.report.GraphDao;
import com.dianping.cat.home.dal.report.GraphEntity;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.page.transaction.Handler.DetailOrder;
import com.dianping.cat.report.page.transaction.Handler.SummaryOrder;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

public class HistoryGraphs {

	public static final double NOTEXIST = -1;

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
		int size = (int) ((end.getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
		double[] total_count = new double[size];
		double[] failure_count = new double[size];
		double[] sum = new double[size];

		for (int i = 0; i < size; i++) {
			total_count[i] = NOTEXIST;
			failure_count[i] = NOTEXIST;
			sum[i] = NOTEXIST;
		}

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(name)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
				String summaryContent = graph.getSummaryContent();
				String[] allLines = summaryContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[SummaryOrder.TYPE.ordinal()].equals(type)) {
						appendArray(total_count, indexOfperiod, records[SummaryOrder.TOTAL_COUNT.ordinal()], 12);
						appendArray(failure_count, indexOfperiod, records[SummaryOrder.FAILURE_COUNT.ordinal()], 12);
						appendArray(sum, indexOfperiod, records[SummaryOrder.SUM.ordinal()], 12);
					}
				}
			}
		} else if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(name)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
				String detailContent = graph.getDetailContent();
				String[] allLines = detailContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[DetailOrder.TYPE.ordinal()].equals(type) && records[DetailOrder.NAME.ordinal()].equals(name)) {
						appendArray(total_count, indexOfperiod, records[DetailOrder.TOTAL_COUNT.ordinal()], 12);
						appendArray(failure_count, indexOfperiod, records[DetailOrder.FAILURE_COUNT.ordinal()], 12);
						appendArray(sum, indexOfperiod, records[DetailOrder.SUM.ordinal()], 12);
					}
				}
			}
		}

		result.put("total_count", total_count);
		result.put("failure_count", failure_count);
		result.put("sum", sum);
		return result;
	}

	public void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String type = payload.getType();
		String name = payload.getName();
		String display = name != null ? name : type;

		int size = (int) ((end.getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
		HistoryGraphItem item = new HistoryGraphItem();
		item.setStart(start);
		item.setSize(size);

		item.setTitles(display + " Response Time (ms)");
		Map<String, double[]> graphData = getGraphDatas(model, payload);
		double[] sum = graphData.get("sum");
		double[] totalCount = graphData.get("total_count");
		double[] avg = new double[sum.length];
		for (int i = 0; i < sum.length; i++) {
			if (totalCount[i] > 0) {
				avg[i] = sum[i] / totalCount[i];
			}
		}
		item.addValue(avg);
		model.setResponseTrend(item.getJsonString());

		item.getValues().clear();
		item.setTitles(display + " Hits (count)");

		item.addValue(totalCount);
		model.setHitTrend(item.getJsonString());

		item.getValues().clear();
		item.setTitles(display + " Error (count)");
		item.addValue(graphData.get("failure_count"));
		model.setErrorTrend(item.getJsonString());
	}

	public Map<String, double[]> getGraphDatas(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ip = model.getIpAddress();
		String queryIp = "All".equals(ip) == true ? "all" : ip;
		List<Graph> graphs = new ArrayList<Graph>();

		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + TimeUtil.ONE_HOUR) {
			try {
				Graph graph = m_graphDao.findSingalByDomainNameIpDuration(new Date(startLong), queryIp, domain,
				      "transaction", GraphEntity.READSET_FULL);
				graphs.add(graph);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		Map<String, double[]> result = buildGraphDatas(start, end, type, name, graphs);
		return result;
	}
}