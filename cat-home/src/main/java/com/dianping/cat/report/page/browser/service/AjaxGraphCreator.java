package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.DistributeDetailInfo;
import com.dianping.cat.report.graph.DistributeDetailInfo.DistributeDetail;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.browser.display.AjaxDataDisplayInfo;
import com.dianping.cat.web.AjaxData;

public class AjaxGraphCreator {
	@Inject
	private AjaxDataBuilder m_dataBuilder;

	@Inject
	private AjaxDataService m_WebApiService;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	public LineChart buildChartData(final Map<String, Double[]> datas, AjaxQueryType type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(type.getTitle());

		if (AjaxQueryType.SUCCESS.getType().equals(type)) {
			lineChart.setMinYlable(lineChart.queryMinYlable(datas));
			lineChart.setMaxYlabel(100D);
		}

		for (Entry<String, Double[]> entry : datas.entrySet()) {
			Double[] data = entry.getValue();

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	public LineChart buildLineChart(AjaxDataQueryEntity queryEntity1, AjaxDataQueryEntity queryEntity2,
	      AjaxQueryType type) {
		Map<String, Double[]> datas = new LinkedHashMap<String, Double[]>();

		if (queryEntity1 != null) {
			Double[] data = m_WebApiService.queryGraphValue(queryEntity1, type);

			datas.put(Constants.CURRENT_STR, data);
		}

		if (queryEntity2 != null) {
			Double[] data = m_WebApiService.queryGraphValue(queryEntity2, type);

			datas.put(Constants.COMPARISION_STR, data);
		}
		return buildChartData(datas, type);
	}

	public AjaxDataDisplayInfo buildAjaxDistributeChart(AjaxDataQueryEntity entity, AjaxDataField field) {
		List<AjaxData> datas = m_dataBuilder.queryByField(entity, field);
		DistributeDetailInfo detailInfos = buildAjaxDistributeDetails(field, datas);
		AjaxDataDisplayInfo info = new AjaxDataDisplayInfo();

		info.setDistributeDetailInfos(detailInfos);
		info.setPieChart(buildPieChart(detailInfos));
		info.setBarChart(buildBarChart(detailInfos, field));
		return info;
	}

	private BarChart buildBarChart(DistributeDetailInfo detailInfos, AjaxDataField field) {
		BarChart barChart = new BarChart();
		barChart.setTitle("加载时间分布");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(field.getName());
		List<DistributeDetail> datas = detailInfos.getItems();

		Collections.sort(datas, new Comparator<DistributeDetail>() {
			@Override
			public int compare(DistributeDetail o1, DistributeDetail o2) {
				return (int) (o2.getDelayAvg() - o1.getDelayAvg());
			}
		});

		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (DistributeDetail data : datas) {
			itemList.add(data.getTitle());
			dataList.add(data.getDelayAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private PieChart buildPieChart(DistributeDetailInfo detailInfos) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();

		for (DistributeDetail detail : detailInfos.getItems()) {
			Item item = new Item();

			item.setTitle(detail.getTitle());
			item.setId(detail.getId());
			item.setNumber(detail.getRequestSum());
			items.add(item);
		}
		pieChart.setTitle("请求量分布");
		pieChart.addItems(items);
		return pieChart;
	}

	private DistributeDetailInfo buildAjaxDistributeDetails(AjaxDataField field, List<AjaxData> datas) {
		DistributeDetailInfo detailInfos = new DistributeDetailInfo();

		for (AjaxData data : datas) {
			DistributeDetail detail = new DistributeDetail();

			Pair<Integer, String> pair = buildPieChartFieldTitlePair(data, field);
			detail.setId(pair.getKey()).setTitle(pair.getValue());
			long requestSum = data.getAccessNumberSum();
			detail.setRequestSum(requestSum);

			if (requestSum > 0) {
				detail.setDelayAvg(data.getResponseSumTimeSum() / requestSum);
			}

			detailInfos.add(detail);
		}

		double sum = 0;

		for (DistributeDetail detail : detailInfos.getItems()) {
			sum += detail.getRequestSum();
		}

		if (sum > 0) {
			for (DistributeDetail detail : detailInfos.getItems()) {
				detail.setRatio(detail.getRequestSum() / sum);
			}
		}

		return detailInfos;
	}

	private Pair<Integer, String> buildPieChartFieldTitlePair(AjaxData data, AjaxDataField field) {
		String title = "Unknown";
		int keyValue = -1;

		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> operators = m_webConfigManager
			      .queryConfigItem(WebConfigManager.OPERATOR);
			com.dianping.cat.configuration.web.entity.Item operator = null;
			keyValue = data.getOperator();

			if (operators != null && (operator = operators.get(keyValue)) != null) {
				title = operator.getName();
			}
			break;
		case CITY:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> cities = m_webConfigManager
			      .queryConfigItem(WebConfigManager.CITY);
			com.dianping.cat.configuration.web.entity.Item city = null;
			keyValue = data.getCity();

			if (cities != null && (city = cities.get(keyValue)) != null) {
				title = city.getName();
			}
			break;
		case NETWORK:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> networks = m_webConfigManager
			      .queryConfigItem(WebConfigManager.NETWORK);
			com.dianping.cat.configuration.web.entity.Item network = null;
			keyValue = data.getNetwork();

			if (networks != null && (network = networks.get(keyValue)) != null) {
				title = network.getName();
			}
			break;
		case CODE:
			Map<Integer, Code> codes = m_patternManager.queryCodes();
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

}
