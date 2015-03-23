package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.AppSpeedDataEntity;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.app.display.AppDataSequence;
import com.dianping.cat.report.page.app.display.AppSpeedDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;

public class AppSpeedService {

	@Inject
	private AppSpeedDataDao m_dao;

	private final static String CURRENT = "当前值";

	private final static String COMPARISION = "对比值";

	private AppSpeedDetail build5MinuteData(int minute, List<AppSpeedData> datas, Date period) {
		long accessSum = 0, slowAccessSum = 0, sum = 0;
		double responseSum = 0, responseAvg = 0, ratio = 0;

		for (AppSpeedData data : datas) {
			accessSum += data.getAccessNumberSum();
			slowAccessSum += data.getSlowAccessNumberSum();
			responseSum += data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
		}
		sum = accessSum + slowAccessSum;

		if (sum > 0) {
			ratio = slowAccessSum * 100.0 / sum;
			responseAvg = responseSum / sum;
		}
		AppSpeedDetail d = new AppSpeedDetail();

		d.setPeriod(period);
		d.setMinuteOrder(minute);
		d.setAccessNumberSum(sum);
		d.setResponseTimeAvg(responseAvg);
		d.setSlowRatio(ratio);
		return d;
	}

	private AppSpeedSequence buildAppSequence(List<AppSpeedData> fromDatas, Date period) {
		Map<Integer, List<AppSpeedData>> dataMap = new LinkedHashMap<Integer, List<AppSpeedData>>();
		int max = -5;

		for (AppSpeedData from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
			List<AppSpeedData> datas = dataMap.get(minute);

			if (datas == null) {
				datas = new LinkedList<AppSpeedData>();

				dataMap.put(minute, datas);
			}
			datas.add(from);
		}
		int n = max / 5 + 1;
		int length = queryAppDataDuration(period, n);

		return new AppSpeedSequence(length, dataMap, period);
	}

	private AppSpeedDisplayInfo buildAppSpeedDisplayInfo(Map<String, AppSpeedSequence> datas) {
		AppSpeedDisplayInfo info = new AppSpeedDisplayInfo();

		info.setLineChart(buildLineChart(datas));
		info.setAppSpeedDetails(buildSpeedDetail(datas));
		info.setAppSpeedSummarys(buildOneDayData(datas));

		return info;
	}

	public LineChart buildLineChart(final Map<String, AppSpeedSequence> datas) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle("延时平均值（毫秒/5分钟）");

		for (Entry<String, AppSpeedSequence> entry : datas.entrySet()) {
			Double[] data = computeDelayAvg(entry.getValue());

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	private Map<String, AppSpeedDetail> buildOneDayData(Map<String, AppSpeedSequence> datas) {
		Map<String, AppSpeedDetail> summarys = new LinkedHashMap<String, AppSpeedDetail>();

		for (Entry<String, AppSpeedSequence> entry : datas.entrySet()) {
			try {
				Map<Integer, List<AppSpeedData>> appSpeedData = entry.getValue().getRecords();
				Date period = entry.getValue().getPeriod();

				if (!appSpeedData.isEmpty()) {
					long accessSum = 0, slowAccessSum = 0, sum = 0;
					double responseSum = 0, responseAvg = 0, ratio = 0;

					for (Entry<Integer, List<AppSpeedData>> e : appSpeedData.entrySet()) {
						for (AppSpeedData data : e.getValue()) {
							accessSum += data.getAccessNumberSum();
							slowAccessSum += data.getSlowAccessNumberSum();
							responseSum += data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
						}
					}
					sum = accessSum + slowAccessSum;
					ratio = slowAccessSum * 100.0 / sum;
					responseAvg = responseSum / sum;
					AppSpeedDetail d = new AppSpeedDetail();

					d.setPeriod(period);
					d.setAccessNumberSum(sum);
					d.setResponseTimeAvg(responseAvg);
					d.setSlowRatio(ratio);
					summarys.put(entry.getKey(), d);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return summarys;
	}

	private Map<String, List<AppSpeedDetail>> buildSpeedDetail(Map<String, AppSpeedSequence> datas) {
		Map<String, List<AppSpeedDetail>> details = new LinkedHashMap<String, List<AppSpeedDetail>>();

		for (Entry<String, AppSpeedSequence> entry : datas.entrySet()) {
			Map<Integer, List<AppSpeedData>> appSpeedDataMap = entry.getValue().getRecords();
			Date period = entry.getValue().getPeriod();
			List<AppSpeedDetail> detail = new ArrayList<AppSpeedDetail>();

			for (Entry<Integer, List<AppSpeedData>> e : appSpeedDataMap.entrySet()) {
				int minute = e.getKey();
				List<AppSpeedData> data = e.getValue();

				if (!data.isEmpty()) {
					detail.add(build5MinuteData(minute, data, period));
				}
			}
			details.put(entry.getKey(), detail);
		}

		return details;
	}

	public AppSpeedDisplayInfo buildSpeedDisplayInfo(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, AppSpeedSequence> datas = queryRawData(queryEntity1, queryEntity2);
		AppSpeedDisplayInfo appSpeedDisplayInfo = buildAppSpeedDisplayInfo(datas);

		return appSpeedDisplayInfo;
	}

	public Double[] computeDelayAvg(AppSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppSpeedData>> entry : convertedData.getRecords().entrySet()) {
			for (AppSpeedData data : entry.getValue()) {
				long count = data.getAccessNumberSum() + data.getSlowAccessNumberSum();
				long sum = data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
				double avg = sum / count;
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = avg;
				}
			}
		}
		return value;
	}

	public Double[] computeRequestCount(AppSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppSpeedData>> entry : convertedData.getRecords().entrySet()) {
			for (AppSpeedData data : entry.getValue()) {
				double count = data.getAccessNumberSum();
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = count;
				}
			}
		}
		return value;
	}

	private int queryAppDataDuration(Date period, int defaultValue) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (period.equals(cal.getTime())) {
			long start = cal.getTimeInMillis();
			long current = System.currentTimeMillis();
			int length = (int) (current - current % 300000 - start) / 300000 - 1;

			return length < 0 ? 0 : length;
		}
		return defaultValue;
	}

	private AppSpeedSequence queryData(SpeedQueryEntity queryEntity) {
		List<AppSpeedData> datas = queryValue(queryEntity);
		AppSpeedSequence sequence = buildAppSequence(datas, queryEntity.getDate());

		return sequence;
	}

	public double queryOneDayDelayAvg(AppSpeedSequence sequence) {
		Double[] values = computeDelayAvg(sequence);
		double delaySum = 0;
		int size = 0;

		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				delaySum += values[i];
				size++;
			}
		}
		return size > 0 ? delaySum / size : -1;
	}

	private Map<String, AppSpeedSequence> queryRawData(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, AppSpeedSequence> datas = new LinkedHashMap<String, AppSpeedSequence>();

		if (queryEntity1 != null) {
			AppSpeedSequence data1 = queryData(queryEntity1);

			if (data1.getDuration() > 0) {
				datas.put(CURRENT, data1);
			}
		}

		if (queryEntity2 != null) {
			AppSpeedSequence data2 = queryData(queryEntity2);

			if (data2.getDuration() > 0) {
				datas.put(COMPARISION, data2);
			}
		}
		return datas;
	}

	public List<AppSpeedData> queryValue(BaseQueryEntity entity) {
		int speedId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int platform = entity.getPlatfrom();
		List<AppSpeedData> datas = new ArrayList<AppSpeedData>();

		try {
			datas = m_dao.findDataByMinute(speedId, period, city, operator, network, appVersion, platform,
			      AppSpeedDataEntity.READSET_AVG_DATA);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public class AppSpeedSequence extends AppDataSequence<AppSpeedData> {

		private Date m_period;

		public AppSpeedSequence(int duration, Map<Integer, List<AppSpeedData>> appSpeedDatas, Date period) {
			super(duration, appSpeedDatas);
			m_period = period;
		}

		public Date getPeriod() {
			return m_period;
		}
	}
}
