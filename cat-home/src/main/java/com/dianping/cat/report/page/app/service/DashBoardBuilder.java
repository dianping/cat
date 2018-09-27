package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppDataField;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.MapChart;
import com.dianping.cat.report.graph.MapChart.Item;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppGraphCreator;
import com.dianping.cat.report.page.app.display.Area;
import com.dianping.cat.report.page.app.display.DashBoardInfo;

public class DashBoardBuilder {

	@Inject
	private JsonBuilder m_jsonBuilder;

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public DashBoardInfo buildDashBoard(final CommandQueryEntity entity) {
		final DashBoardInfo dashboard = new DashBoardInfo();
		ExecutorService executor = Executors.newFixedThreadPool(6);

		executor.execute(new Runnable() {
			@Override
			public void run() {
				List<AppDataDetail> cities = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.CITY,
				      QueryType.NETWORK_SUCCESS);
				dashboard.setMapChart(buildResponseMapChart(cities));
				dashboard.setSuccessMapChart(buildSuccessMapChart(cities));
			}

		});

		executor.execute(new Runnable() {
			@Override
			public void run() {
				List<AppDataDetail> operators = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.OPERATOR,
				      QueryType.NETWORK_SUCCESS);
				dashboard.setOperatorChart(buildResponseBarChart(operators, AppDataField.OPERATOR));
				dashboard.setOperatorSuccessChart(buildSuccessRatioBarChart(operators, AppDataField.OPERATOR));
			}
		});

		executor.execute(new Runnable() {
			@Override
			public void run() {
				List<AppDataDetail> version = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.APP_VERSION,
				      QueryType.NETWORK_SUCCESS);
				version = buildTops(version, 15);
				dashboard.setVersionChart(buildResponseBarChart(version, AppDataField.APP_VERSION));
				dashboard.setVersionSuccessChart(buildSuccessRatioBarChart(version, AppDataField.APP_VERSION));
			}
		});

		executor.execute(new Runnable() {
			@Override
			public void run() {
				List<AppDataDetail> platform = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.PLATFORM,
				      QueryType.NETWORK_SUCCESS);
				dashboard.setPlatformChart(buildResponseBarChart(platform, AppDataField.PLATFORM));
				dashboard.setPlatformSuccessChart(buildSuccessRatioBarChart(platform, AppDataField.PLATFORM));
			}
		});

		executor.execute(new Runnable() {
			@Override
			public void run() {
				LineChart lineChart = buildLineChart(entity, QueryType.DELAY);
				dashboard.setLineChart(lineChart);
			}
		});

		executor.execute(new Runnable() {
			@Override
			public void run() {
				LineChart lineChart = buildLineChart(entity, QueryType.NETWORK_SUCCESS);
				lineChart.setMinYlable(95.0);
				dashboard.setSuccessLineChart(lineChart);
			}
		});

		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Cat.logError(e);
		}

		return dashboard;
	}

	private LineChart buildLineChart(CommandQueryEntity entity, QueryType type) {
		List<AppCommandData> datas = m_appDataService.queryByMinute(entity, type);
		int size = (entity.getEndMinuteOrder() - entity.getStartMinuteOrder()) / 5 + 1;
		Double[] data = null;

		switch (type) {
		case NETWORK_SUCCESS:
			data = buildSuccessRatios(datas, size, entity);
			break;
		case DELAY:
			data = buildDelays(datas, size, entity);
			break;
		default:
			throw new RuntimeException("Unsupported Type");
		}

		LineChart lineChart = new LineChart();
		Date start = new Date(entity.getDate().getTime() + entity.getStartMinuteOrder() * TimeHelper.ONE_MINUTE);

		lineChart.setSize(size);
		lineChart.setStep(TimeHelper.ONE_MINUTE * 5);
		lineChart.setStart(start);
		lineChart.add(Constants.CURRENT_STR, data);
		return lineChart;
	}

	private Double[] buildSuccessRatios(List<AppCommandData> datas, int size, CommandQueryEntity entity) {
		Double[] data = new Double[size];
		Map<Integer, List<AppCommandData>> dataMap = buildDataMap(datas);

		for (Entry<Integer, List<AppCommandData>> entry : dataMap.entrySet()) {
			int index = (entry.getKey() - entity.getStartMinuteOrder()) / 5;
			data[index] = computeSuccessRatio(entity.getId(), entry.getValue());
		}
		return data;
	}

	private double computeSuccessRatio(int commandId, List<AppCommandData> datas) {
		long success = 0;
		long sum = 0;

		for (AppCommandData data : datas) {
			long number = data.getAccessNumberSum();

			if (m_appConfigManager.isSuccessCode(commandId, data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private Map<Integer, List<AppCommandData>> buildDataMap(List<AppCommandData> datas) {
		Map<Integer, List<AppCommandData>> dataMap = new LinkedHashMap<Integer, List<AppCommandData>>();

		for (AppCommandData data : datas) {
			int minute = data.getMinuteOrder();
			List<AppCommandData> list = dataMap.get(minute);

			if (list == null) {
				list = new LinkedList<AppCommandData>();

				dataMap.put(minute, list);
			}
			list.add(data);
		}
		return dataMap;
	}

	private Double[] buildDelays(List<AppCommandData> datas, int size, CommandQueryEntity entity) {
		Double[] data = new Double[size];

		for (AppCommandData commandData : datas) {
			long count = commandData.getAccessNumberSum();
			long sum = commandData.getResponseSumTimeSum();

			if (count > 0) {
				double avg = sum / count;
				int index = (commandData.getMinuteOrder() - entity.getStartMinuteOrder()) / 5;

				if (index < size) {
					data[index] = avg;
				}
			}
		}
		return data;
	}

	private List<AppDataDetail> buildTops(List<AppDataDetail> version, int count) {
		Collections.sort(version, new Comparator<AppDataDetail>() {
			@Override
			public int compare(AppDataDetail o1, AppDataDetail o2) {
				return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
			}
		});
		List<AppDataDetail> tops = new ArrayList<AppDataDetail>();
		int index = 0;

		for (AppDataDetail detail : version) {
			tops.add(detail);
			index++;

			if (index >= count) {
				break;
			}
		}
		return tops;
	}

	private BarChart buildSuccessRatioBarChart(List<AppDataDetail> datas, AppDataField field) {
		BarChart barChart = new BarChart();
		barChart.setyAxis("成功率");
		barChart.setSerieName(field.getName());
		Collections.sort(datas, new Comparator<AppDataDetail>() {
			@Override
			public int compare(AppDataDetail o1, AppDataDetail o2) {
				if (o2.getSuccessRatio() > o1.getSuccessRatio()) {
					return 1;
				} else if (o2.getSuccessRatio() < o1.getSuccessRatio()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (AppDataDetail data : datas) {
			itemList.add(queryItemName(data, field));
			dataList.add(data.getSuccessRatio());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private BarChart buildResponseBarChart(List<AppDataDetail> datas, AppDataField field) {
		BarChart barChart = new BarChart();
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(field.getName());
		Collections.sort(datas, new Comparator<AppDataDetail>() {
			@Override
			public int compare(AppDataDetail o1, AppDataDetail o2) {
				if (o2.getResponseTimeAvg() > o1.getResponseTimeAvg()) {
					return 1;
				} else if (o2.getResponseTimeAvg() < o1.getResponseTimeAvg()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (AppDataDetail data : datas) {
			itemList.add(queryItemName(data, field));
			dataList.add(data.getResponseTimeAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private String queryItemName(AppDataDetail data, AppDataField field) {
		String title = null;
		int value = 0;

		switch (field) {
		case OPERATOR:
			value = data.getOperator();
			break;
		case APP_VERSION:
			value = data.getAppVersion();
			break;
		case PLATFORM:
			value = data.getPlatform();
			break;
		default:
			throw new RuntimeException("Unsupported AppDataField.");
		}
		com.dianping.cat.configuration.mobile.entity.Item item = m_mobileConfigManager.queryConstantItem(
		      field.getTitle(), value);

		if (item != null) {
			title = item.getValue();
		} else {
			title = String.valueOf(value);
		}
		return title;
	}

	private MapChart buildResponseMapChart(List<AppDataDetail> cities) {
		List<Item> relayItems = new ArrayList<Item>();

		for (AppDataDetail appDataDetail : cities) {
			String province = Area.CHINA_PROVINCE.get(appDataDetail.getCity());
			relayItems.add(new Item(province, appDataDetail.getResponseTimeAvg()));
		}
		return buildMapChart(relayItems, "", 0, 3000);
	}

	private MapChart buildSuccessMapChart(List<AppDataDetail> cities) {
		List<Item> relayItems = new ArrayList<Item>();

		for (AppDataDetail appDataDetail : cities) {
			String province = Area.CHINA_PROVINCE.get(appDataDetail.getCity());
			relayItems.add(new Item(province, appDataDetail.getSuccessRatio()));
		}
		return buildMapChart(relayItems, "", 98, 100);
	}

	private MapChart buildMapChart(List<Item> requestItems, String title, int min, int max) {
		MapChart mapChart = new MapChart();
		mapChart.setTitle(title);
		mapChart.setMax(max);
		mapChart.setMin(min);
		mapChart.setDataSeries(requestItems);
		mapChart.setData(m_jsonBuilder.toJson(requestItems));
		return mapChart;
	}
}
