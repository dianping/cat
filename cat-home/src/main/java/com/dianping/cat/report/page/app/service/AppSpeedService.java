package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.AppSpeedDataEntity;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.display.AppSpeedDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;

@Named
public class AppSpeedService {

	@Inject
	private AppSpeedDataDao m_dao;

	@Inject
	private AppSpeedDataBuilder m_dataBuilder;

	private AppSpeedDetail build5MinuteData(int minute, AppSpeedData data, Date period) {
		long accessSum = 0, slowAccessSum = 0, sum = 0;
		double responseSum = 0, responseAvg = 0, ratio = 0;

		accessSum += data.getAccessNumberSum();
		slowAccessSum += data.getSlowAccessNumberSum();
		responseSum += data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
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
		lineChart.setHtmlTitle(QueryType.DELAY.getTitle());

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
				Map<Integer, AppSpeedData> appSpeedData = entry.getValue().getRecords();
				Date period = entry.getValue().getPeriod();

				if (!appSpeedData.isEmpty()) {
					long accessSum = 0, slowAccessSum = 0, sum = 0;
					double responseSum = 0, responseAvg = 0, ratio = 0;

					for (Entry<Integer, AppSpeedData> e : appSpeedData.entrySet()) {
						AppSpeedData data = e.getValue();
						accessSum += data.getAccessNumberSum();
						slowAccessSum += data.getSlowAccessNumberSum();
						responseSum += data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
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
			Map<Integer, AppSpeedData> speeds = entry.getValue().getRecords();
			Date period = entry.getValue().getPeriod();
			List<AppSpeedDetail> detail = new ArrayList<AppSpeedDetail>();

			for (Entry<Integer, AppSpeedData> e : speeds.entrySet()) {
				int minute = e.getKey();

				detail.add(build5MinuteData(minute, e.getValue(), period));
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

	public AppSpeedDisplayInfo buildBarCharts(SpeedQueryEntity queryEntity) {
		return m_dataBuilder.buildBarChart(queryEntity);
	}

	public Double[] computeDelayAvg(AppSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, AppSpeedData> entry : convertedData.getRecords().entrySet()) {
			AppSpeedData data = entry.getValue();
			long count = data.getAccessNumberSum() + data.getSlowAccessNumberSum();
			long sum = data.getResponseSumTimeSum() + data.getSlowResponseSumTimeSum();
			double avg = sum / count;
			int index = data.getMinuteOrder() / 5;

			if (index < n) {
				value[index] = avg;
			}
		}
		return value;
	}

	public Double[] computeRequestCount(AppSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, AppSpeedData> entry : convertedData.getRecords().entrySet()) {
			AppSpeedData data = entry.getValue();
			double count = data.getAccessNumberSum();
			int index = data.getMinuteOrder() / 5;

			if (index < n) {
				value[index] = count;
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
		int speedId = queryEntity.getId();
		Date period = queryEntity.getDate();
		int city = queryEntity.getCity();
		int operator = queryEntity.getOperator();
		int network = queryEntity.getNetwork();
		int appVersion = queryEntity.getVersion();
		int platform = queryEntity.getPlatfrom();
		List<AppSpeedData> datas = new ArrayList<AppSpeedData>();
		Map<Integer, AppSpeedData> records = new LinkedHashMap<Integer, AppSpeedData>();
		int max = -5;

		try {
			datas = m_dao.findDataByMinute(speedId, period, city, operator, network, appVersion, platform,
			      AppSpeedDataEntity.READSET_AVG_DATA);
		} catch (Exception e) {
			Cat.logError(e);
		}

		for (AppSpeedData data : datas) {
			int minute = data.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
			records.put(minute, data);
		}
		int n = max / 5 + 1;
		int length = queryAppDataDuration(period, n);

		return new AppSpeedSequence(period, length, records);
	}

	private Map<String, AppSpeedSequence> queryRawData(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, AppSpeedSequence> datas = new LinkedHashMap<String, AppSpeedSequence>();

		if (queryEntity1 != null) {
			AppSpeedSequence data = queryData(queryEntity1);

			if (data.getDuration() > 0) {
				datas.put(Constants.CURRENT_STR, data);
			}
		}

		if (queryEntity2 != null) {
			AppSpeedSequence data = queryData(queryEntity2);

			if (data.getDuration() > 0) {
				datas.put(Constants.COMPARISION_STR, data);
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

	public class AppSpeedSequence {

		private Date m_period;

		protected int m_duration;

		protected Map<Integer, AppSpeedData> m_records;

		public AppSpeedSequence(Date period, int duration, Map<Integer, AppSpeedData> reocords) {
			m_period = period;
			m_duration = duration;
			m_records = reocords;
		}

		public int getDuration() {
			return m_duration;
		}

		public Map<Integer, AppSpeedData> getRecords() {
			return m_records;
		}

		public Date getPeriod() {
			return m_period;
		}
	}
}
