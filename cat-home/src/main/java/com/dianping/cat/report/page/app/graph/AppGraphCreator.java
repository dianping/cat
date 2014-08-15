package com.dianping.cat.report.page.app.graph;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
		List<Double[]> datas = new LinkedList<Double[]>();

		if (queryEntity1 != null) {
			Double[] data1 = prepareQueryData(queryEntity1, type);
			datas.add(data1);
		}

		if (queryEntity2 != null) {
			Double[] values2 = prepareQueryData(queryEntity2, type);
			datas.add(values2);
		}
		return buildChartData(datas, type);
	}

	private Double[] prepareQueryData(QueryEntity queryEntity, String type) {
		Double[] value = m_appDataService.queryValue(queryEntity, type);

		return value;
	}

	private String queryType(String type) {
		if (AppDataService.SUCCESS.equals(type)) {
			return "成功率（%/5分钟）";
		} else if (AppDataService.REQUEST.equals(type)) {
			return "请求数（个/5分钟）";
		} else if (AppDataService.DELAY.equals(type)) {
			return "延时平均值（毫秒/5分钟）";
		} else {
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	public LineChart buildChartData(final List<Double[]> datas, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(queryType(type));
		int length = datas.size();

		for (int i = 0; i < length; i++) {
			Double[] data = datas.get(i);

			lineChart.add("查询" + (i + 1), data);
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
