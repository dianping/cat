package com.dianping.cat.report.page.app.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataGroupByField;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.app.PieChartDetailInfo;

public class AppGraphCreator extends AbstractGraphCreator {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppConfigManager m_manager;

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

		if (AppDataService.SUCCESS.equals(type)) {
			lineChart.setMinYlable(95D);
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

	@Override
	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();
		long time = startTime;

		for (int i = 0; i < length; i++) {
			time += step * TimeHelper.ONE_MINUTE;
			map.put(time, data[i]);
		}
		return map;
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

	public Pair<PieChart, List<PieChartDetailInfo>> buildPieChart(QueryEntity entity, AppDataGroupByField field) {
		List<PieChartDetailInfo> infos = new LinkedList<PieChartDetailInfo>();
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<AppDataCommand> datas = m_appDataService.queryAppDataCommandsByField(entity, field);

		for (AppDataCommand data : datas) {
			Pair<Integer, Item> pair = buildPieChartItem(entity.getCommand(), data, field);
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

	private Pair<Integer, String> buildPieChartFieldTitlePair(int command, AppDataCommand data, AppDataGroupByField field) {
		String title = "Unknown";
		int keyValue = -1;

		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> operators = m_manager
			      .queryConfigItem(AppConfigManager.OPERATOR);
			com.dianping.cat.configuration.app.entity.Item operator = null;
			keyValue = data.getOperator();

			if (operators != null && (operator = operators.get(keyValue)) != null) {
				title = operator.getName();
			}
			break;
		case APP_VERSION:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> appVersions = m_manager
			      .queryConfigItem(AppConfigManager.VERSION);
			com.dianping.cat.configuration.app.entity.Item appVersion = null;
			keyValue = data.getAppVersion();

			if (appVersions != null && (appVersion = appVersions.get(keyValue)) != null) {
				title = appVersion.getName();
			}
			break;
		case CITY:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> cities = m_manager
			      .queryConfigItem(AppConfigManager.CITY);
			com.dianping.cat.configuration.app.entity.Item city = null;
			keyValue = data.getCity();

			if (cities != null && (city = cities.get(keyValue)) != null) {
				title = city.getName();
			}
			break;
		case CONNECT_TYPE:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> connectTypes = m_manager
			      .queryConfigItem(AppConfigManager.CONNECT_TYPE);
			com.dianping.cat.configuration.app.entity.Item connectType = null;
			keyValue = data.getConnnectType();

			if (connectTypes != null && (connectType = connectTypes.get(keyValue)) != null) {
				title = connectType.getName();
			}
		case NETWORK:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> networks = m_manager
			      .queryConfigItem(AppConfigManager.NETWORK);
			com.dianping.cat.configuration.app.entity.Item network = null;
			keyValue = data.getNetwork();

			if (networks != null && (network = networks.get(keyValue)) != null) {
				title = network.getName();
			}
			break;
		case PLATFORM:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> platforms = m_manager
			      .queryConfigItem(AppConfigManager.PLATFORM);
			com.dianping.cat.configuration.app.entity.Item platform = null;
			keyValue = data.getPlatform();

			if (platforms != null && (platform = platforms.get(keyValue)) != null) {
				title = platform.getName();
			}
			break;
		case CODE:
			Map<Integer, Code> codes = m_manager.queryCodeByCommand(command);
			Code code = null;
			keyValue = data.getCode();

			if (codes != null && (code = codes.get(keyValue)) != null) {
				title = code.getName();
			}
			break;
		default:
			throw new RuntimeException("Unrecognized groupby field: " + field);
		}
		if ("Unknown".equals(title)) {
			title += " [ " + keyValue + " ]";
		}
		return new Pair<Integer, String>(keyValue, title);
	}

	private Pair<Integer, Item> buildPieChartItem(int command, AppDataCommand data, AppDataGroupByField field) {
		Item item = new Item();

		item.setNumber(data.getAccessNumberSum());
		Pair<Integer, String> pair = buildPieChartFieldTitlePair(command, data, field);

		item.setTitle(pair.getValue());
		return new Pair<Integer, Item>(pair.getKey(), item);
	}
}
