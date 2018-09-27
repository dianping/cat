package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.browser.display.WebSpeedDetail;
import com.dianping.cat.report.page.browser.display.WebSpeedDisplayInfo;
import com.dianping.cat.web.WebSpeedData;

public class WebSpeedService extends ContainerHolder {

	@Inject
	private WebSpeedDataBuilder m_dataBuilder;

	private WebSpeedDetail build5MinuteData(int minute, WebSpeedData data, Date period) {
		double responseAvg = 0.0;
		long accessSum = data.getAccessNumberSum();
		double responseSum = data.getResponseSumTimeSum();

		if (accessSum > 0) {
			responseAvg = responseSum / accessSum;
		}

		WebSpeedDetail d = new WebSpeedDetail();

		d.setPeriod(period);
		d.setMinuteOrder(minute);
		d.setAccessNumberSum(accessSum);
		d.setResponseTimeAvg(responseAvg);
		return d;
	}

	public LineChart buildLineChart(final Map<String, WebSpeedSequence> datas) {
		LineChart lineChart = new LineChart();
		lineChart.setId("web");
		lineChart.setUnit("");
		lineChart.setHtmlTitle("延时平均值（毫秒/5分钟）");

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			Double[] data = computeDelayAvg(entry.getValue());

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	private Map<String, WebSpeedDetail> buildSpeedSummary(Map<String, WebSpeedSequence> datas) {
		Map<String, WebSpeedDetail> summarys = new LinkedHashMap<String, WebSpeedDetail>();

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			try {
				Map<Integer, WebSpeedData> appSpeedData = entry.getValue().getRecords();
				Date period = entry.getValue().getPeriod();

				if (!appSpeedData.isEmpty()) {
					long accessSum = 0;
					double responseSum = 0, responseAvg = 0;

					for (Entry<Integer, WebSpeedData> e : appSpeedData.entrySet()) {
						accessSum += e.getValue().getAccessNumberSum();
						responseSum += e.getValue().getResponseSumTimeSum();
					}

					if (accessSum > 0) {
						responseAvg = responseSum / accessSum;
					}

					WebSpeedDetail d = new WebSpeedDetail();

					d.setPeriod(period);
					d.setAccessNumberSum(accessSum);
					d.setResponseTimeAvg(responseAvg);
					summarys.put(entry.getKey(), d);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return summarys;
	}

	private Map<String, List<WebSpeedDetail>> buildSpeedDetail(Map<String, WebSpeedSequence> datas) {
		Map<String, List<WebSpeedDetail>> details = new LinkedHashMap<String, List<WebSpeedDetail>>();

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			Map<Integer, WebSpeedData> appSpeedDataMap = entry.getValue().getRecords();
			Date period = entry.getValue().getPeriod();
			List<WebSpeedDetail> detail = new ArrayList<WebSpeedDetail>();

			for (Entry<Integer, WebSpeedData> e : appSpeedDataMap.entrySet()) {
				int minute = e.getKey();
				WebSpeedData data = e.getValue();
				
				if (data != null) {
					detail.add(build5MinuteData(minute, data, period));
				}
			}
			details.put(entry.getKey(), detail);
		}

		return details;
	}

	public WebSpeedDisplayInfo buildSpeedDisplayInfo(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, WebSpeedSequence> datas = queryRawData(queryEntity1, queryEntity2);
		WebSpeedDisplayInfo appSpeedDisplayInfo = buildWebSpeedDisplayInfo(datas);

		return appSpeedDisplayInfo;
	}

	public WebSpeedDisplayInfo buildBarCharts(SpeedQueryEntity queryEntity) {
		return m_dataBuilder.buildChart(queryEntity);
	}

	private WebSpeedSequence buildWebSequence(List<WebSpeedData> fromDatas, Date period) {
		Map<Integer, WebSpeedData> dataMap = new LinkedHashMap<Integer, WebSpeedData>();
		int max = -5;

		for (WebSpeedData data : fromDatas) {
			int minute = data.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}

			dataMap.put(minute, data);
		}

		int n = max / 5 + 1;
		int length = queryWebDataDuration(period, n);

		return new WebSpeedSequence(period, length, dataMap);
	}

	private WebSpeedDisplayInfo buildWebSpeedDisplayInfo(Map<String, WebSpeedSequence> datas) {
		WebSpeedDisplayInfo info = new WebSpeedDisplayInfo();

		info.setLineChart(buildLineChart(datas));
		info.setWebSpeedDetails(buildSpeedDetail(datas));
		info.setWebSpeedSummarys(buildSpeedSummary(datas));

		return info;
	}

	public Double[] computeDelayAvg(WebSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, WebSpeedData> entry : convertedData.getRecords().entrySet()) {
			WebSpeedData data = entry.getValue();
			long count = data.getAccessNumberSum();
			long sum = data.getResponseSumTimeSum();
			double avg = 0;

			if (count > 0) {
				avg = sum / count;
			}

			int index = data.getMinuteOrder() / 5;

			if (index < n) {
				value[index] = avg;
			}
		}
		return value;
	}

	private WebSpeedSequence queryData(SpeedQueryEntity queryEntity) {
		List<WebSpeedData> datas = m_dataBuilder.queryValueByTime(queryEntity);

		return buildWebSequence(datas, queryEntity.getDate());
	}

	private Map<String, WebSpeedSequence> queryRawData(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, WebSpeedSequence> datas = new LinkedHashMap<String, WebSpeedSequence>();

		if (queryEntity1 != null) {
			WebSpeedSequence data1 = queryData(queryEntity1);

			if (data1.getDuration() > 0) {
				datas.put(Constants.CURRENT_STR, data1);
			}
		}

		if (queryEntity2 != null) {
			WebSpeedSequence data2 = queryData(queryEntity2);

			if (data2.getDuration() > 0) {
				datas.put(Constants.COMPARISION_STR, data2);
			}
		}
		return datas;
	}

	private int queryWebDataDuration(Date period, int defaultValue) {
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

	protected class WebSpeedSequence {

		private Date m_period;

		protected int m_duration;

		protected Map<Integer, WebSpeedData> m_records;

		public WebSpeedSequence(Date period, int duration, Map<Integer, WebSpeedData> records) {
			m_period = period;
			m_duration = duration;
			m_records = records;
		}

		public int getDuration() {
			return m_duration;
		}

		public Date getPeriod() {
			return m_period;
		}

		public Map<Integer, WebSpeedData> getRecords() {
			return m_records;
		}
	}

}
