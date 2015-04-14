package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.core.dal.DailyGraph;
import com.dianping.cat.core.dal.DailyGraphDao;
import com.dianping.cat.core.dal.DailyGraphEntity;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.core.dal.GraphDao;
import com.dianping.cat.core.dal.GraphEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.BaseHistoryGraphs;
import com.dianping.cat.report.page.problem.Handler.DetailOrder;
import com.dianping.cat.report.page.problem.Handler.SummaryOrder;

public class HistoryGraphs extends BaseHistoryGraphs {

	private static final String ERROR = "errors";

	@Inject
	private GraphDao m_graphDao;

	@Inject
	private DailyGraphDao m_dailyGraphDao;

	private LineChart buildFail(List<Map<String, double[]>> datas, Date start, long step, int size, String queryType) {
		LineChart item = new LineChart();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);

		for (Map<String, double[]> data : datas) {
			item.addValue(data.get(ERROR));
		}
		item.setSubTitles(buildSubTitle(start, size, step, queryType));
		return item;
	}

	public Map<String, double[]> buildGraphDatasForDaily(Date start, Date end, String type, String name,
	      List<DailyGraph> graphs) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_DAY);
		double[] errorCount = new double[size];

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(name)) {
			for (DailyGraph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeHelper.ONE_DAY);
				String summaryContent = graph.getSummaryContent();
				String[] allLines = summaryContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[SummaryOrder.TYPE.ordinal()].equals(type)) {
						errorCount[indexOfperiod] = Double.valueOf(records[SummaryOrder.TOTAL_COUNT.ordinal()]);
					}
				}
			}
		} else if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(name)) {
			for (DailyGraph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeHelper.ONE_DAY);
				String detailContent = graph.getDetailContent();
				String[] allLines = detailContent.split("\n");
				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records[DetailOrder.TYPE.ordinal()].equals(type)
					      && records[DetailOrder.STATUS.ordinal()].equals(name)) {
						errorCount[indexOfperiod] = Double.valueOf(records[DetailOrder.TOTAL_COUNT.ordinal()]);
					}
				}
			}
		}

		result.put(ERROR, errorCount);
		return result;
	}

	public Map<String, double[]> buildGraphDatasFromHour(Date start, Date end, String type, String status,
	      List<Graph> graphs) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		int size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_HOUR) * 60;
		double[] errors = new double[size];

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(status)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeHelper.ONE_HOUR * 60);
				String summaryContent = graph.getSummaryContent();
				String[] allLines = summaryContent.split("\n");

				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");

					if (records.length < SummaryOrder.values().length) {
						continue;
					}
					String dbType = records[SummaryOrder.TYPE.ordinal()];

					if (dbType.equals(type)) {
						String[] values = records[SummaryOrder.DETAIL.ordinal()].split(",");
						for (int k = 0; k < values.length; k++) {
							errors[indexOfperiod + k] = Double.parseDouble(values[k]);
						}
					}
				}
			}
		} else if (!StringUtils.isEmpty(type) && !StringUtils.isEmpty(status)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeHelper.ONE_HOUR * 60);
				String detailContent = graph.getDetailContent();
				String[] allLines = detailContent.split("\n");

				for (int j = 0; j < allLines.length; j++) {
					String[] records = allLines[j].split("\t");
					if (records.length < DetailOrder.values().length) {
						continue;
					}
					String dbStatus = records[DetailOrder.STATUS.ordinal()];
					String dbType = records[DetailOrder.TYPE.ordinal()];

					if (status.equals(dbStatus) && type.equals(dbType)) {
						String[] values = records[DetailOrder.DETAIL.ordinal()].split(",");
						for (int k = 0; k < values.length; k++) {
							errors[indexOfperiod + k] = Double.parseDouble(values[k]);
						}
					}
				}
			}
		}
		result.put(ERROR, errors);
		return result;
	}

	public void buildGroupTrendGraph(Model model, Payload payload, List<String> ips) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getStatus();
		int size = (int) ((end.getTime() - start.getTime()) * 12 / TimeHelper.ONE_HOUR);
		long step = TimeHelper.ONE_MINUTE * 5;
		String queryType = payload.getReportType();

		if (queryType.equalsIgnoreCase("month")) {
			size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_DAY);
			step = TimeHelper.ONE_DAY;
		}

		List<Map<String, double[]>> allDatas = null;
		for (String ip : ips) {
			List<Map<String, double[]>> datas = buildLineChartData(start, end, domain, type, name, ip, queryType);

			if (allDatas == null) {
				allDatas = datas;
			} else {
				mergerList(allDatas, datas);
			}
		}
		LineChart item = buildFail(allDatas, start, step, size, queryType);
		model.setErrorsTrend(item.getJsonString());
	}

	private List<Map<String, double[]>> buildLineChartData(Date start, Date end, String domain, String type,
	      String name, String ip, String queryType) {
		List<Map<String, double[]>> allDatas = new ArrayList<Map<String, double[]>>();
		if (queryType.equalsIgnoreCase("day")) {
			Map<String, double[]> currentGraph = getGraphDatasFromHour(start, end, domain, type, name, ip);
			Map<String, double[]> lastDayGraph = getGraphDatasFromHour(new Date(start.getTime() - TimeHelper.ONE_DAY),
			      new Date(end.getTime() - TimeHelper.ONE_DAY), domain, type, name, ip);
			Map<String, double[]> lastWeekGraph = getGraphDatasFromHour(new Date(start.getTime() - TimeHelper.ONE_WEEK),
			      new Date(end.getTime() - TimeHelper.ONE_WEEK), domain, type, name, ip);

			allDatas.add(currentGraph);
			allDatas.add(lastDayGraph);
			allDatas.add(lastWeekGraph);
		} else if (queryType.equalsIgnoreCase("week")) {
			Map<String, double[]> currentGraph = getGraphDatasFromDaily(start, end, domain, type, name, ip);
			Map<String, double[]> lastWeek = getGraphDatasFromDaily(new Date(start.getTime() - TimeHelper.ONE_WEEK),
			      new Date(end.getTime() - TimeHelper.ONE_WEEK), domain, type, name, ip);

			allDatas.add(currentGraph);
			allDatas.add(lastWeek);
		} else if (queryType.equalsIgnoreCase("month")) {
			Map<String, double[]> graphData = getGraphDatasFromDaily(start, end, domain, type, name, ip);

			allDatas.add(graphData);
		} else {
			throw new RuntimeException("Error graph query type");
		}
		return allDatas;
	}

	public void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) * 60 / TimeHelper.ONE_HOUR);
		String queryType = payload.getReportType();
		String domain = model.getDomain();
		String type = payload.getType();
		String name = payload.getStatus();
		String ip = model.getIpAddress();

		long step = TimeHelper.ONE_MINUTE;
		if (queryType.equalsIgnoreCase("week")) {
			size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_DAY);
			step = TimeHelper.ONE_DAY;
		} else if (queryType.equalsIgnoreCase("month")) {
			size = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_DAY);
			step = TimeHelper.ONE_DAY;
		}

		List<Map<String, double[]>> allDatas = buildLineChartData(start, end, domain, type, name, ip, queryType);
		LineChart item = buildFail(allDatas, start, step, size, queryType);
		model.setErrorsTrend(item.getJsonString());
	}

	private Map<String, double[]> getGraphDatasFromDaily(Date start, Date end, String domain, String type, String name,
	      String ip) {
		String queryIp = "All".equalsIgnoreCase(ip) == true ? "All" : ip;
		List<DailyGraph> graphs = new ArrayList<DailyGraph>();

		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + TimeHelper.ONE_DAY) {
			try {
				DailyGraph graph = m_dailyGraphDao.findByDomainNameIpDate(new Date(startLong), queryIp, domain,
				      ProblemAnalyzer.ID, DailyGraphEntity.READSET_FULL);
				graphs.add(graph);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return buildGraphDatasForDaily(start, end, type, name, graphs);
	}

	public Map<String, double[]> getGraphDatasFromHour(Date start, Date end, String domain, String type, String name,
	      String ip) {
		String queryIP = "All".equals(ip) == true ? "all" : ip;
		List<Graph> graphs = new ArrayList<Graph>();

		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + TimeHelper.ONE_HOUR) {
			try {
				Graph graph = m_graphDao.findSingalByDomainNameIpDuration(new Date(startLong), queryIP, domain,
				      ProblemAnalyzer.ID, GraphEntity.READSET_FULL);
				graphs.add(graph);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return buildGraphDatasFromHour(start, end, type, name, graphs);
	}

}
