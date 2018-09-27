package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.AppSpeedDataEntity;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;
import com.dianping.cat.configuration.mobile.entity.Item;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.page.app.display.AppSpeedDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.site.lookup.util.StringUtils;

@Named
public class AppSpeedDataBuilder {

	@Inject
	private AppSpeedDataDao m_dao;

	@Inject
	private AppCommandConfigManager m_appConfig;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public AppSpeedDisplayInfo buildBarChart(SpeedQueryEntity entity) {
		AppSpeedDisplayInfo info = new AppSpeedDisplayInfo();

		buildBarChart(entity, new CityDataBuilder(), info);
		buildBarChart(entity, new NetworkDataBuilder(), info);
		buildBarChart(entity, new PlatformDataBuilder(), info);
		buildBarChart(entity, new OperatorDataBuilder(), info);
		buildBarChart(entity, new VersionDataBuilder(), info);

		return info;
	}

	private void buildBarChart(SpeedQueryEntity entity, BarChartDataBuilder builder, AppSpeedDisplayInfo info) {
		BarChart barChart = new BarChart();
		barChart.setTitle(builder.getChartTitle());
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(builder.getSerieName());

		List<AppSpeedDetail> datas = queryValues(entity, builder);
		buildBarChartDatas(barChart, datas);

		builder.enrichAppSpeedDisplayInfo(info, barChart, datas);
	}

	private void buildBarChartDatas(BarChart barChart, List<AppSpeedDetail> datas) {
		Collections.sort(datas, new Comparator<AppSpeedDetail>() {
			@Override
			public int compare(AppSpeedDetail o1, AppSpeedDetail o2) {
				return (int) (o2.getResponseTimeAvg() - o1.getResponseTimeAvg());
			}
		});

		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (AppSpeedDetail data : datas) {
			itemList.add(data.getItemName());
			dataList.add(data.getResponseTimeAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
	}

	private List<AppSpeedDetail> queryValues(SpeedQueryEntity entity, BarChartDataBuilder builder) {
		List<AppSpeedDetail> details = new ArrayList<AppSpeedDetail>();

		try {
			List<AppSpeedData> datas = builder.queryRawData(entity);

			for (AppSpeedData appSpeedData : datas) {
				AppSpeedDetail detail = buildAppSpeedDetail(appSpeedData);

				Item item = builder.queryConfigItem(appSpeedData);

				if (StringUtils.isNotEmpty(item.getValue())) {
					detail.setItemName(item.getValue());
				} else {
					detail.setItemName(String.valueOf(item.getId()));
				}

				details.add(detail);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return details;
	}

	private AppSpeedDetail buildAppSpeedDetail(AppSpeedData data) {
		AppSpeedDetail detail = new AppSpeedDetail();
		double avg = 0.0;
		long accessNumberSum = data.getAccessNumberSum();

		if (accessNumberSum > 0) {
			avg = data.getResponseSumTimeSum() / accessNumberSum;
		}
		detail.setAccessNumberSum(accessNumberSum);
		detail.setResponseTimeAvg(avg);

		return detail;
	}

	abstract class BarChartDataBuilder {

		abstract String getChartTitle();

		abstract String getSerieName();

		abstract Item queryConfigItem(AppSpeedData data);

		abstract List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException;

		abstract void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details);
	}

	class CityDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(省份)";
		}

		@Override
		String getSerieName() {
			return "省份列表";
		}

		@Override
		Item queryConfigItem(AppSpeedData data) {
			Item item = m_mobileConfigManager.queryConstantItem(MobileConstants.CITY, data.getCity());

			if (item == null) {
				item = new Item(data.getCity());
			}

			return item;
		}

		@Override
		List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException {
			return m_dao.findDataByCity(entity.getId(), entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getVersion(), entity.getPlatfrom(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), AppSpeedDataEntity.READSET_CITY_DATA);
		}

		@Override
		void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details) {
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
		String getSerieName() {
			return "网络类型列表";
		}

		@Override
		Item queryConfigItem(AppSpeedData data) {
			Item item = m_mobileConfigManager.queryConstantItem(MobileConstants.NETWORK, data.getNetwork());

			if (item == null) {
				item = new Item(data.getNetwork());
			}

			return item;
		}

		@Override
		List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException {
			return m_dao.findDataByNetwork(entity.getId(), entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getVersion(), entity.getPlatfrom(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), AppSpeedDataEntity.READSET_NETWORK_DATA);
		}

		@Override
		void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details) {
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
		String getSerieName() {
			return "运营商列表";
		}

		@Override
		Item queryConfigItem(AppSpeedData data) {
			Item item = m_mobileConfigManager.queryConstantItem(MobileConstants.OPERATOR, data.getOperator());

			if (item == null) {
				item = new Item(data.getOperator());
			}

			return item;
		}

		@Override
		List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException {
			return m_dao.findDataByOperator(entity.getId(), entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getVersion(), entity.getPlatfrom(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), AppSpeedDataEntity.READSET_OPERATOR_DATA);
		}

		@Override
		void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details) {
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
		String getSerieName() {
			return "平台列表";
		}

		@Override
		Item queryConfigItem(AppSpeedData data) {
			Item item = m_mobileConfigManager.queryConstantItem(MobileConstants.PLATFORM, data.getPlatform());

			if (item == null) {
				item = new Item(data.getPlatform());
			}

			return item;
		}

		@Override
		List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException {
			return m_dao.findDataByPlatform(entity.getId(), entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getVersion(), entity.getPlatfrom(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), AppSpeedDataEntity.READSET_PLATFORM_DATA);
		}

		@Override
		void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details) {
			info.setPlatformChart(barChart);
			info.addDetail("平台", details);
		}
	}

	class VersionDataBuilder extends BarChartDataBuilder {

		@Override
		String getChartTitle() {
			return "请求平均时间(版本)";
		}

		@Override
		String getSerieName() {
			return "版本列表";
		}

		@Override
		Item queryConfigItem(AppSpeedData data) {
			Item item = m_mobileConfigManager.queryConstantItem(MobileConstants.VERSION, data.getAppVersion());

			if (item == null) {
				item = new Item(data.getAppVersion());
			}

			return item;
		}

		@Override
		List<AppSpeedData> queryRawData(SpeedQueryEntity entity) throws DalException {
			return m_dao.findDataByVersion(entity.getId(), entity.getDate(), entity.getCity(), entity.getOperator(),
			      entity.getNetwork(), entity.getVersion(), entity.getPlatfrom(), entity.getStartMinuteOrder(),
			      entity.getEndMinuteOrder(), AppSpeedDataEntity.READSET_VERSION_DATA);
		}

		@Override
		void enrichAppSpeedDisplayInfo(AppSpeedDisplayInfo info, BarChart barChart, List<AppSpeedDetail> details) {
			info.setVersionChart(barChart);
			info.addDetail("版本", details);
		}
	}
}
