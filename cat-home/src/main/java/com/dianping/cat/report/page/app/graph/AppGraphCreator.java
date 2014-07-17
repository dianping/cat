package com.dianping.cat.report.page.app.graph;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

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
		double[] data1 = prepareAllData(queryEntity1, type);
		dataList.add(data1);

		if (queryEntity2 != null) {
			double[] values2 = prepareAllData(queryEntity2, type);
			dataList.add(values2);
		}

		return buildChartData(dataList, type);
	}

	private double[] prepareAllData(QueryEntity queryEntity, String type) {
		double[] value = m_appDataService.queryValue(queryEntity, type);

		return value;
	}

	public LineChart buildChartData(final LinkedList<double[]> dataList, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setHtmlTitle(type);
		int i = 1;

		for (double[] data : dataList) {
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

	public class AppDataServiceMock extends AppDataService {
		public double[] queryValue(QueryEntity entity, String type) {
			if (SUCCESS_RATIO.equals(type)) {
				return querySuccessRatio(entity);
			} else if (REQUEST_COUNT.equals(type)) {
				return queryRequestCount(entity);
			} else if (DELAY_AVG.equals(type)) {
				return queryDelayAvg(entity);
			} else {
				return null;
			}
		}

		private double[] makeMockValue(String type) {
			long startTime = TimeUtil.getCurrentDay().getTime();
			long current = System.currentTimeMillis();
			long endTime = current - current % 300000;
			int n = (int) (endTime - startTime) / 300000;
			double[] value = new double[n];

			for (int i = 0; i < n; i++) {
				value[i] = (new Random().nextDouble() + 1) * 100;
			}
			return value;
		}

		private double[] querySuccessRatio(QueryEntity entity) {

			return makeMockValue(SUCCESS_RATIO);
		}

		private double[] queryDelayAvg(QueryEntity entity) {

			return makeMockValue(DELAY_AVG);
		}

		private double[] queryRequestCount(QueryEntity entity) {

			return makeMockValue(REQUEST_COUNT);
		}
	}
}
