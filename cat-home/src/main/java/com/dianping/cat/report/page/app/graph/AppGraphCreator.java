package com.dianping.cat.report.page.app.graph;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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

	public LineChart buildLineChart(QueryEntity queryEntity, String type) {
		Map<String, double[]> values = prepareAllData(queryEntity, type);

		long startTime = queryEntity.getDate().getTime();
		long endTime = startTime + TimeUtil.ONE_DAY;
		Date endDate = new Date(endTime);
		Date startDate = new Date(startTime);

		return buildChartData(values, startDate, endDate);
	}

	private Map<String, double[]> prepareAllData(QueryEntity queryEntity, String type) {
		Map<String, double[]> value = m_appDataService.queryValue(queryEntity, type);

		return value;
	}

	public LineChart buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate) {
		LineChart lineChart = new LineChart();

		for (Entry<String, double[]> entry : datas.entrySet()) {
			String key = entry.getKey();

			lineChart.setId(startDate.toString());
			lineChart.setHtmlTitle(key);

			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 5);

			lineChart.add(startDate.toString(), all);
		}
		return lineChart;
	}

	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();
		long time = startTime;
		int i = 0;

		for (; i < length; i++) {
			time += step * TimeUtil.ONE_MINUTE;
			map.put(time, data[i]);
		}

		return map;
	}

	public class AppDataServiceMock extends AppDataService {
		public Map<String, double[]> queryValue(QueryEntity entity, String type) {
			if (SUCCESS_RATIO.equals(type)) {
				return querySuccessRatio(entity);
			} else if (REQUEST_COUNT.equals(type)) {
				return queryRequestCount(entity);
			} else if (DELAY_AVG.equals(type)) {
				return queryDelayAvg(entity);
			} else {
				return new LinkedHashMap<String, double[]>();
			}
		}

		private Map<String, double[]> makeMockValue(String type) {
			Map<String, double[]> map = new LinkedHashMap<String, double[]>();
			long startTime = TimeUtil.getCurrentDay().getTime();
			long current = System.currentTimeMillis();
			long endTime = current - current % 300000;
			int n = (int) (endTime - startTime) / 300000;
			double[] value = new double[n];

			for (int i = 0; i < n; i++) {
				value[i] = (new Random().nextDouble() + 1) * 100;
			}
			map.put(type, value);
			return map;
		}

		private Map<String, double[]> querySuccessRatio(QueryEntity entity) {

			return makeMockValue(SUCCESS_RATIO);
		}

		private Map<String, double[]> queryDelayAvg(QueryEntity entity) {

			return makeMockValue(DELAY_AVG);
		}

		private Map<String, double[]> queryRequestCount(QueryEntity entity) {

			return makeMockValue(REQUEST_COUNT);
		}
	}
	
}
