package com.dianping.cat.report.page.app.graph;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;

public class AppGraphCreator extends AbstractGraphCreator {

	@Inject
	private AppDataService m_appDataService;

	public LineChart buildLineChart(QueryEntity queryEntity1, QueryEntity queryEntity2, String type) {
		LinkedList<double[]> dataList = new LinkedList<double[]>();

		if (queryEntity1 != null) {
			double[] data1 = prepareQueryData(queryEntity1, type);
			dataList.add(data1);
		}

		if (queryEntity2 != null) {
			double[] values2 = prepareQueryData(queryEntity2, type);
			dataList.add(values2);
		}
		return buildChartData(dataList, type);
	}

	private double[] prepareQueryData(QueryEntity queryEntity, String type) {
		double[] value = m_appDataService.queryValue(queryEntity, type);

		return value;
	}

	private String queryType(String type) {
		if (AppDataService.SUCCESS.equals(type)) {
			return "成功率";
		} else if (AppDataService.REQUEST.equals(type)) {
			return "请求数";
		} else if (AppDataService.DELAY.equals(type)) {
			return "成功延时(ms)";
		} else {
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	public LineChart buildChartData(final LinkedList<double[]> dataList, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setHtmlTitle(queryType(type));
		int length = dataList.size();

		for (int i = 0; i < length; i++) {
			double[] data = dataList.get(i);
			lineChart.add("查询" + i++, data);
		}
		return lineChart;
	}

	@Override
	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();
		long time = startTime;

		for (int i = 0; i < length; i++) {
			time += step * TimeUtil.ONE_MINUTE;
			map.put(time, data[i]);
		}
		return map;
	}
}
