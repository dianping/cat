package com.dianping.cat.report.page.browser.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.Readset;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.configuration.web.entity.Item;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.page.browser.display.WebSpeedDetail;
import com.dianping.cat.report.page.browser.display.WebSpeedDisplayInfo;
import com.dianping.cat.web.WebSpeedData;
import com.dianping.cat.web.WebSpeedDataDao;
import com.dianping.cat.web.WebSpeedDataEntity;

public class WebSpeedDataBuilder {

	@Inject
	private WebSpeedDataDao m_dao;

	@Inject
	private WebSpeedConfigManager m_speedConfig;

	@Inject
	private WebConfigManager m_webConfig;

	private void buildBarChartDatas(BarChart barChart, List<WebSpeedDetail> datas) {
		Collections.sort(datas, new Comparator<WebSpeedDetail>() {
			@Override
			public int compare(WebSpeedDetail o1, WebSpeedDetail o2) {
				return (int) (o2.getResponseTimeAvg() - o1.getResponseTimeAvg());
			}
		});

		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (WebSpeedDetail data : datas) {
			itemList.add(data.getItemName());
			dataList.add(data.getResponseTimeAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
	}

	public WebSpeedDisplayInfo buildChart(SpeedQueryEntity entity) {
		WebSpeedDisplayInfo info = new WebSpeedDisplayInfo();

		buildChart(entity, new CityDataBuilder(), info);
		buildChart(entity, new NetworkDataBuilder(), info);
		buildChart(entity, new PlatformDataBuilder(), info);
		buildChart(entity, new OperatorDataBuilder(), info);
		buildChart(entity, new SourceDataBuilder(), info);

		return info;
	}

	private void buildChart(SpeedQueryEntity entity, BarChartDataBuilder builder, WebSpeedDisplayInfo info) {
		BarChart barChart = new BarChart();
		barChart.setTitle(builder.getChartTitle());
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(builder.getSerieName());

		List<WebSpeedDetail> datas = queryValues(entity, builder);
		buildBarChartDatas(barChart, datas);

		builder.enrichWebSpeedDisplayInfo(info, barChart, datas);

	}

	private WebSpeedDetail buildWebSpeedDetail(WebSpeedData data) {
		WebSpeedDetail detail = new WebSpeedDetail();
		double avg = 0.0;
		long accessNumberSum = data.getAccessNumberSum();

		if (accessNumberSum > 0) {
			avg = data.getResponseSumTimeSum() / accessNumberSum;
		}
		detail.setAccessNumberSum(accessNumberSum);
		detail.setResponseTimeAvg(avg);

		return detail;
	}

	private List<WebSpeedDetail> buildWebSpeedDetails(List<WebSpeedData> datas, BarChartDataBuilder builder) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();

		for (WebSpeedData webSpeedData : datas) {
			WebSpeedDetail detail = buildWebSpeedDetail(webSpeedData);

			Item item = builder.queryConfigItem(webSpeedData);

			if (StringUtils.isNotEmpty(item.getName())) {
				detail.setItemName(item.getName());
			} else {
				detail.setItemName(item.getId().toString());
			}
			details.add(detail);
		}
		return details;
	}

	private void enrichWebSpeedData(List<WebSpeedData> datas, int stepId) throws NoSuchMethodException,
	      IllegalAccessException, InvocationTargetException {

		for (WebSpeedData webSpeedData : datas) {
			try {
				Method getResponseSumTimeSum = webSpeedData.getClass().getMethod("getResponseSumTimeSum" + stepId);
				long responseSumTimeSum = (Long) getResponseSumTimeSum.invoke(webSpeedData);

				Method getAccessNumberSum = webSpeedData.getClass().getMethod("getAccessNumberSum" + stepId);
				long accessNumberSum = (Long) getAccessNumberSum.invoke(webSpeedData);

				webSpeedData.setAccessNumberSum(accessNumberSum);
				webSpeedData.setResponseSumTimeSum(responseSumTimeSum);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByTime(SpeedQueryEntity entity) {
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());

		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_AVG_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByMinute(pageId, period, city, operator, network, platform, source, readset);

				enrichWebSpeedData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValues(SpeedQueryEntity entity, BarChartDataBuilder builder) {
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();

		if (pageId >= 0 && stepId > 0) {
			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField(builder.getReadSetPrefix() + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				List<WebSpeedData> datas = builder.queryRawData(pageId, entity, readset);

				enrichWebSpeedData(datas, stepId);

				return buildWebSpeedDetails(datas, builder);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return new ArrayList<WebSpeedDetail>();
	}

	abstract class BarChartDataBuilder {

		abstract String getChartTitle();

		abstract String getReadSetPrefix();

		abstract String getSerieName();

		abstract Item queryConfigItem(WebSpeedData data);

		abstract List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException;

		abstract void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details);
	}

	class CityDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(省份)";
		}

		@Override
		String getReadSetPrefix() {
			return "READSET_CITY_DATA";
		}

		@Override
		String getSerieName() {
			return "省份列表";
		}

		@Override
		Item queryConfigItem(WebSpeedData data) {
			return m_webConfig.queryItem(WebConfigManager.CITY, data.getCity());
		}

		@Override
		List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException {
			return m_dao.findDataByCity(pageId, entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getPlatform(), entity.getSource(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), readset);
		}

		@Override
		void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details) {
			info.setCityChart(barChart);
			info.addDetail("省份", details);
		}
	}

	class NetworkDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(网络类型)";
		}

		@Override
		String getReadSetPrefix() {
			return "READSET_NETWORK_DATA";
		}

		@Override
		String getSerieName() {
			return "网络类型列表";
		}

		@Override
		Item queryConfigItem(WebSpeedData data) {
			return m_webConfig.queryItem(WebConfigManager.NETWORK, data.getNetwork());
		}

		@Override
		List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException {
			return m_dao.findDataByNetwork(pageId, entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getPlatform(), entity.getSource(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), readset);
		}

		@Override
		void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details) {
			info.setNetworkChart(barChart);
			info.addDetail("网络类型", details);
		}
	}

	class OperatorDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(运营商)";
		}

		@Override
		String getReadSetPrefix() {
			return "READSET_OPERATOR_DATA";
		}

		@Override
		String getSerieName() {
			return "运营商列表";
		}

		@Override
		Item queryConfigItem(WebSpeedData data) {
			return m_webConfig.queryItem(WebConfigManager.OPERATOR, data.getOperator());
		}

		@Override
		List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException {
			return m_dao.findDataByOperator(pageId, entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getPlatform(), entity.getSource(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), readset);
		}

		@Override
		void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details) {
			info.setOperatorChart(barChart);
			info.addDetail("运营商", details);
		}
	}

	class PlatformDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(平台)";
		}

		@Override
		String getReadSetPrefix() {
			return "READSET_PLATFORM_DATA";
		}

		@Override
		String getSerieName() {
			return "平台列表";
		}

		@Override
		Item queryConfigItem(WebSpeedData data) {
			return m_webConfig.queryItem(WebConfigManager.PLATFORM, data.getPlatform());
		}

		@Override
		List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException {
			return m_dao.findDataByPlatform(pageId, entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getPlatform(), entity.getSource(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), readset);
		}

		@Override
		void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details) {
			info.setPlatformChart(barChart);
			info.addDetail("平台", details);
		}
	}

	class SourceDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(来源)";
		}

		@Override
		String getReadSetPrefix() {
			return "READSET_SOURCE_DATA";
		}

		@Override
		String getSerieName() {
			return "来源列表";
		}

		@Override
		Item queryConfigItem(WebSpeedData data) {
			return m_webConfig.queryItem(WebConfigManager.SOURCE, data.getSource());
		}

		@Override
		List<WebSpeedData> queryRawData(int pageId, SpeedQueryEntity entity, Readset<WebSpeedData> readset)
		      throws DalException {
			return m_dao.findDataBySource(pageId, entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getPlatform(), entity.getSource(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), readset);
		}

		@Override
		void enrichWebSpeedDisplayInfo(WebSpeedDisplayInfo info, BarChart barChart, List<WebSpeedDetail> details) {
			info.setSourceChart(barChart);
			info.addDetail("来源", details);
		}
	}
}
