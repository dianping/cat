package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailygraph;
import com.dianping.cat.home.dal.report.DailygraphDao;
import com.dianping.cat.home.dal.report.DailygraphEntity;
import com.dianping.cat.home.dal.report.Graph;
import com.dianping.cat.home.dal.report.GraphDao;
import com.dianping.cat.home.dal.report.GraphEntity;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.page.transaction.Handler.DetailOrder;
import com.dianping.cat.report.page.transaction.Handler.SummaryOrder;

public class HistoryGraphs {

	public static final double NOTEXIST = -1;

	@Inject
	private GraphDao m_graphDao;

	@Inject
	private DailygraphDao m_dailyGraphDao;

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

	public Map<String, double[]> buildGraphDatasForHour(Date start, Date end, String type, String name,
	      List<Graph> graphs) {
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

	public Map<String, double[]> buildGraphDatasForDaily(Date start, Date end, String type, String name,
	      List<Dailygraph> graphs) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_DAY);
		double[] total_count = new double[size];
		double[] failure_count = new double[size];
		double[] sum = new double[size];

		for (int i = 0; i < size; i++) {
			total_count[i] = NOTEXIST;
			failure_count[i] = NOTEXIST;
			sum[i] = NOTEXIST;
		}

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(name)) {
			for (Dailygraph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeUtil.ONE_DAY);
				String summaryContent = graph.getSummaryContent();
				String[] allLines = summaryContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[SummaryOrder.TYPE.ordinal()].equals(type)) {
						total_count[indexOfperiod] = Double.valueOf(records[SummaryOrder.TOTAL_COUNT.ordinal()]);
						failure_count[indexOfperiod] = Double.valueOf(records[SummaryOrder.FAILURE_COUNT.ordinal()]);
						sum[indexOfperiod] = Double.valueOf(records[SummaryOrder.SUM.ordinal()]);
					}
				}
			}
		} else if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(name)) {
			for (Dailygraph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeUtil.ONE_DAY);
				String detailContent = graph.getDetailContent();
				String[] allLines = detailContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[DetailOrder.TYPE.ordinal()].equals(type) && records[DetailOrder.NAME.ordinal()].equals(name)) {
						total_count[indexOfperiod] = Double.valueOf(records[DetailOrder.TOTAL_COUNT.ordinal()]);
						failure_count[indexOfperiod] = Double.valueOf(records[DetailOrder.FAILURE_COUNT.ordinal()]);
						sum[indexOfperiod] = Double.valueOf(records[DetailOrder.SUM.ordinal()]);
					}
				}
			}
		}

		result.put("total_count", total_count);
		result.put("failure_count", failure_count);
		result.put("sum", sum);
		return result;
	}

	private HistoryGraphItem buildAvg(List<Map<String, double[]>> datas, Date start, int size, long step, String name) {
		HistoryGraphItem item = new HistoryGraphItem();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);
		item.setTitles(name + " Response Time (ms)");

		for (Map<String, double[]> data : datas) {
			double[] sum = data.get("sum");
			double[] totalCount = data.get("total_count");
			double[] avg = new double[sum.length];
			for (int i = 0; i < sum.length; i++) {
				if (totalCount[i] > 0) {
					avg[i] = sum[i] / totalCount[i];
				}
			}
			item.addValue(avg);
		}
		return item;
	}

	private HistoryGraphItem buildTotal(List<Map<String, double[]>> datas, Date start, int size, long step, String name) {
		HistoryGraphItem item = new HistoryGraphItem();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);
		item.setTitles(name + " Hits (count)");

		for (Map<String, double[]> data : datas) {
			double[] totalCount = data.get("total_count");
			item.addValue(totalCount);
		}
		return item;
	}

	private HistoryGraphItem buildFail(List<Map<String, double[]>> datas, Date start, int size, long step, String name) {
		HistoryGraphItem item = new HistoryGraphItem();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);
		item.setTitles(name + " Error (count)");

		for (Map<String, double[]> data : datas) {
			item.addValue(data.get("failure_count"));
		}
		return item;
	}

	public void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String type = payload.getType();
		String name = payload.getName();
		String display = name != null ? name : type;
		int size = (int) ((end.getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
		String queryType = payload.getReportType();
		List<Map<String, double[]>> allDatas = new ArrayList<Map<String, double[]>>();
		long step = TimeUtil.ONE_MINUTE * 5;

		if (queryType.equalsIgnoreCase("day")) {
			Map<String, double[]> currentGraph = getGraphDatasFromHour(start, end, model, payload);
			Map<String, double[]> lastDayGraph = getGraphDatasFromHour(new Date(start.getTime() - TimeUtil.ONE_DAY),
			      new Date(end.getTime() - TimeUtil.ONE_DAY), model, payload);
			Map<String, double[]> lastWeekGraph = getGraphDatasFromHour(new Date(start.getTime() - TimeUtil.ONE_WEEK),
			      new Date(end.getTime() - TimeUtil.ONE_WEEK), model, payload);

			allDatas.add(currentGraph);
			allDatas.add(lastDayGraph);
			allDatas.add(lastWeekGraph);
		} else if (queryType.equalsIgnoreCase("week")) {
			Map<String, double[]> currentGraph = getGraphDatasFromHour(start, end, model, payload);
			Map<String, double[]> lastWeek = getGraphDatasFromHour(new Date(start.getTime() - TimeUtil.ONE_WEEK),
			      new Date(end.getTime() - TimeUtil.ONE_WEEK), model, payload);

			allDatas.add(currentGraph);
			allDatas.add(lastWeek);
		} else if (queryType.equalsIgnoreCase("month")) {
			size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_DAY);
			step = TimeUtil.ONE_DAY;
			Map<String, double[]> graphData = getGraphDatasFromDaily(start, end, model, payload);

			allDatas.add(graphData);
		} else {
			throw new RuntimeException("Error graph query type");
		}
		HistoryGraphItem item = buildAvg(allDatas, start, size, step, display);
		model.setResponseTrend(item.getJsonString());

		item = buildTotal(allDatas, start, size, step, display);
		model.setHitTrend(item.getJsonString());

		item = buildFail(allDatas, start, size, step, display);
		model.setErrorTrend(item.getJsonString());
	}

	private Map<String, double[]> getGraphDatasFromDaily(Date start, Date end, Model model, Payload payload) {
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getName();
		String ip = model.getIpAddress();
		String queryIp = "All".equalsIgnoreCase(ip) == true ? "All" : ip;
		List<Dailygraph> graphs = new ArrayList<Dailygraph>();

		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + TimeUtil.ONE_DAY) {
			try {
				Dailygraph graph = m_dailyGraphDao.findSingalByDomainNameIpDuration(new Date(startLong), queryIp, domain,
						"transaction", DailygraphEntity.READSET_FULL);
				graphs.add(graph);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				e.printStackTrace();
				//Cat.logError(e);
			}
		}
		Map<String, double[]> result = buildGraphDatasForDaily(start, end, type, name, graphs);
		return result;
	}

	public Map<String, double[]> getGraphDatasFromHour(Date start, Date end, Model model, Payload payload) {
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
				e.printStackTrace();
				//Cat.logError(e);
			}
		}
		Map<String, double[]> result = buildGraphDatasForHour(start, end, type, name, graphs);
		return result;
	}
}