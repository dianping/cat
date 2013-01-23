package com.dianping.cat.report.page.problem;

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
import com.dianping.cat.report.page.problem.Handler.DetailOrder;
import com.dianping.cat.report.page.problem.Handler.SummaryOrder;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

public class HistoryGraphs {

	private static final String ERROR = "errors";

	@Inject
	private GraphDao m_graphDao;

	private HistoryGraphItem buildFail(List<Map<String, double[]>> datas, Date start, int size) {
		HistoryGraphItem item = new HistoryGraphItem();

		item.setStart(start);
		item.setSize(size);

		for (Map<String, double[]> data : datas) {
			item.addValue(data.get(ERROR));
		}
		return item;
	}

	public void buildTrendGraph(Model model, Payload payload) {
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		int size = (int) ((end.getTime() - start.getTime()) * 12 / TimeUtil.ONE_HOUR);
		String queryType = payload.getReportType();
		List<Map<String, double[]>> allDatas = new ArrayList<Map<String, double[]>>();
		
		if (queryType.equalsIgnoreCase("day")) {
			Map<String, double[]> currentGraph = getGraphDatas(start, end, model, payload);
			Map<String, double[]> lastDayGraph = getGraphDatas(new Date(start.getTime() - TimeUtil.ONE_DAY),
			      new Date(end.getTime() - TimeUtil.ONE_DAY), model, payload);
			Map<String, double[]> lastWeekGraph = getGraphDatas(new Date(start.getTime() - TimeUtil.ONE_WEEK), new Date(
			      end.getTime() - TimeUtil.ONE_WEEK), model, payload);

			allDatas.add(currentGraph);
			allDatas.add(lastDayGraph);
			allDatas.add(lastWeekGraph);
		} else if (queryType.equalsIgnoreCase("week")) {
			Map<String, double[]> currentGraph = getGraphDatas(start, end, model, payload);
			Map<String, double[]> lastWeek = getGraphDatas(new Date(start.getTime() - TimeUtil.ONE_WEEK),
			      new Date(end.getTime() - TimeUtil.ONE_WEEK), model, payload);

			allDatas.add(currentGraph);
			allDatas.add(lastWeek);
		} else if (queryType.equalsIgnoreCase("month")) {
			Map<String, double[]> graphData = getGraphDatas(start, end, model, payload);
			
			allDatas.add(graphData);
		} else {
			throw new RuntimeException("Error graph query type");
		}
		HistoryGraphItem item =buildFail(allDatas, start, size);
		model.setErrorsTrend(item.getJsonString());
	}
	
	public Map<String, double[]> buildGraphDatas(Date start, Date end, String type, String status, List<Graph> graphs) {
		Map<String, double[]> result = new HashMap<String, double[]>();
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_HOUR) * 60;
		double[] errors = new double[size];

		if (!StringUtils.isEmpty(type) && StringUtils.isEmpty(status)) {
			for (Graph graph : graphs) {
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeUtil.ONE_HOUR * 60);
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
				int indexOfperiod = (int) ((graph.getPeriod().getTime() - start.getTime()) / TimeUtil.ONE_HOUR * 60);
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

	public Map<String, double[]> getGraphDatas(Date start,Date end ,Model model, Payload payload) {
		String domain = model.getDomain();
		String type = payload.getType();
		String status = payload.getStatus();
		String ip = model.getIpAddress();
		String queryIP = "All".equals(ip) == true ? "all" : ip;
		List<Graph> graphs = new ArrayList<Graph>();

		for (long startLong = start.getTime(); startLong < end.getTime(); startLong = startLong + TimeUtil.ONE_HOUR) {
			try {
				Graph graph = m_graphDao.findSingalByDomainNameIpDuration(new Date(startLong), queryIP, domain, "problem",
				      GraphEntity.READSET_FULL);
				graphs.add(graph);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		Map<String, double[]> result = buildGraphDatas(start, end, type, status, graphs);
		return result;
	}

}
