package com.dianping.cat.report.page.web.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.app.WebApiData;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.web.service.WebApiField;
import com.dianping.cat.report.page.web.service.WebApiQueryEntity;
import com.dianping.cat.report.page.web.service.WebApiService;

public class WebGraphCreator {

	@Inject
	private WebApiService m_WebApiService;

	@Inject
	private AppConfigManager m_appConfigManager;

	public LineChart buildChartData(final List<Double[]> datas, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(queryType(type));

		if (WebApiService.SUCCESS.equals(type)) {
			lineChart.setMinYlable(lineChart.queryMinYlable(datas));
			lineChart.setMaxYlabel(100D);
		}

		for (int i = 0; i < datas.size(); i++) {
			Double[] data = datas.get(i);

			if (i == 0) {
				lineChart.add("当前值", data);
			} else if (i == 1) {
				lineChart.add("对比值", data);
			}
		}
		return lineChart;
	}

	public LineChart buildLineChart(WebApiQueryEntity queryEntity1, WebApiQueryEntity queryEntity2, String type) {
		List<Double[]> datas = new LinkedList<Double[]>();

		if (queryEntity1 != null) {
			Double[] data1 = m_WebApiService.queryValue(queryEntity1, type);

			datas.add(data1);
		}

		if (queryEntity2 != null) {
			Double[] values2 = m_WebApiService.queryValue(queryEntity2, type);
			datas.add(values2);
		}
		return buildChartData(datas, type);
	}

	public Pair<PieChart, List<PieChartDetailInfo>> buildPieChart(WebApiQueryEntity entity, WebApiField field) {
		List<PieChartDetailInfo> infos = new LinkedList<PieChartDetailInfo>();
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<WebApiData> datas = m_WebApiService.queryByField(entity, field);

		for (WebApiData data : datas) {
			Pair<Integer, Item> pair = buildPieChartItem(entity.getId(), data, field);
			Item item = pair.getValue();
			PieChartDetailInfo info = new PieChartDetailInfo();

			info.setId(pair.getKey()).setTitle(item.getTitle()).setRequestSum(item.getNumber());
			infos.add(info);
			items.add(item);
		}
		pieChart.setTitle(field.getName() + "访问情况");
		pieChart.addItems(items);
		updatePieChartDetailInfo(infos);

		return new Pair<PieChart, List<PieChartDetailInfo>>(pieChart, infos);
	}

	private Pair<Integer, String> buildPieChartFieldTitlePair(int command, WebApiData data, WebApiField field) {
		String title = "Unknown";
		int keyValue = -1;

		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> operators = m_appConfigManager
			      .queryConfigItem(AppConfigManager.OPERATOR);
			com.dianping.cat.configuration.app.entity.Item operator = null;
			keyValue = data.getOperator();

			if (operators != null && (operator = operators.get(keyValue)) != null) {
				title = operator.getName();
			}
			break;
		case CITY:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> cities = m_appConfigManager
			      .queryConfigItem(AppConfigManager.CITY);
			com.dianping.cat.configuration.app.entity.Item city = null;
			keyValue = data.getCity();

			if (cities != null && (city = cities.get(keyValue)) != null) {
				title = city.getName();
			}
			break;
		case CODE:
			Map<Integer, Code> codes = m_appConfigManager.queryCodeByCommand(command);
			Code code = null;
			keyValue = data.getCode();

			if (codes != null && (code = codes.get(keyValue)) != null) {
				title = code.getName();
				int status = code.getStatus();
				if (status == 0) {
					title = "<span class='text-success'>【成功】</span>" + title;
				} else {
					title = "<span class='text-error'>【失败】</span>" + title;
				}
			}
			break;
		}
		if ("Unknown".equals(title)) {
			title += " [ " + keyValue + " ]";
		}
		return new Pair<Integer, String>(keyValue, title);
	}

	private Pair<Integer, Item> buildPieChartItem(int command, WebApiData data, WebApiField field) {
		Item item = new Item();
		Pair<Integer, String> pair = buildPieChartFieldTitlePair(command, data, field);

		item.setTitle(pair.getValue());
		item.setId(pair.getKey());
		item.setNumber(data.getAccessNumberSum());
		return new Pair<Integer, Item>(pair.getKey(), item);
	}

	private String queryType(String type) {
		if (WebApiService.SUCCESS.equals(type)) {
			return "成功率（%/5分钟）";
		} else if (WebApiService.REQUEST.equals(type)) {
			return "请求数（个/5分钟）";
		} else if (WebApiService.DELAY.equals(type)) {
			return "延时平均值（毫秒/5分钟）";
		} else {
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	private void updatePieChartDetailInfo(List<PieChartDetailInfo> items) {
		double sum = 0;

		for (PieChartDetailInfo item : items) {
			sum += item.getRequestSum();
		}

		if (sum > 0) {
			for (PieChartDetailInfo item : items) {
				item.setSuccessRatio(item.getRequestSum() / sum);
			}
		}
	}
}
