package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.report.page.DataSequence;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;
import com.dianping.cat.report.page.browser.display.AjaxDataDetail;
import com.dianping.cat.web.AjaxData;

public class AjaxDataService {

	@Inject
	private AjaxDataBuilder m_dataBuilder;

	@Inject
	private UrlPatternConfigManager m_urlConfigManager;

	public List<AjaxDataDetail> buildAjaxDataDetailInfos(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxDataDetail> infos = new LinkedList<AjaxDataDetail>();
		List<AjaxData> datas = m_dataBuilder.queryByFieldCode(entity, groupByField);
		Map<Integer, List<AjaxData>> field2Datas = buildFields2Datas(datas, groupByField);

		for (Entry<Integer, List<AjaxData>> entry : field2Datas.entrySet()) {
			List<AjaxData> datalst = entry.getValue();
			AjaxDataDetail info = new AjaxDataDetail();
			double ratio = computeSuccessRatio(datalst);

			info.setSuccessRatio(ratio);
			updateAjaxDataDetailInfo(info, entry, groupByField, entity);
			infos.add(info);
		}
		return infos;
	}

	private Map<Integer, List<AjaxData>> buildDataMap(List<AjaxData> datas) {
		Map<Integer, List<AjaxData>> dataMap = new LinkedHashMap<Integer, List<AjaxData>>();

		for (AjaxData data : datas) {
			int minute = data.getMinuteOrder();
			List<AjaxData> list = dataMap.get(minute);

			if (list == null) {
				list = new LinkedList<AjaxData>();
				dataMap.put(minute, list);
			}
			list.add(data);
		}
		return dataMap;
	}

	private DataSequence<AjaxData> buildAjaxDataSequence(List<AjaxData> fromDatas, Date period) {
		Map<Integer, List<AjaxData>> dataMap = buildDataMap(fromDatas);
		int max = -5;

		for (AjaxData from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
		}
		int n = max / 5 + 1;
		int length = queryAjaxDataDuration(period, n);

		return new DataSequence<AjaxData>(length, dataMap);
	}

	private Map<Integer, List<AjaxData>> buildFields2Datas(List<AjaxData> datas, AjaxDataField field) {
		Map<Integer, List<AjaxData>> field2Datas = new HashMap<Integer, List<AjaxData>>();

		for (AjaxData data : datas) {
			int fieldValue = queryFieldValue(data, field);
			List<AjaxData> lst = field2Datas.get(fieldValue);

			if (lst == null) {
				lst = new ArrayList<AjaxData>();
				field2Datas.put(fieldValue, lst);
			}
			lst.add(data);
		}
		return field2Datas;
	}

	public Double[] computeDelayAvg(DataSequence<AjaxData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
			for (AjaxData data : entry.getValue()) {
				long count = data.getAccessNumberSum();
				long sum = data.getResponseSumTimeSum();
				double avg = sum / count;
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = avg;
				}
			}
		}
		return value;
	}

	public Double[] computeRequestCount(DataSequence<AjaxData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
			for (AjaxData data : entry.getValue()) {
				double count = data.getAccessNumberSum();
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = count;
				}
			}
		}
		return value;
	}

	public Double[] computeSuccessRatio(DataSequence<AjaxData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (int i = 0; i < n; i++) {
			value[i] = 100.0;
		}

		try {
			for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
				int key = entry.getKey();
				int index = key / 5;

				if (index < n) {
					value[index] = computeSuccessRatio(entry.getValue());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return value;
	}

	private double computeSuccessRatio(List<AjaxData> datas) {
		long success = 0;
		long sum = 0;

		for (AjaxData data : datas) {
			long number = data.getAccessNumberSum();

			if (m_urlConfigManager.isSuccessCode(data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private int queryAjaxDataDuration(Date period, int defaultValue) {
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

	private int queryFieldValue(AjaxData data, AjaxDataField field) {
		switch (field) {
		case OPERATOR:
			return data.getOperator();
		case CITY:
			return data.getCity();
		case NETWORK:
			return data.getNetwork();
		case CODE:
		default:
			return CommandQueryEntity.DEFAULT_VALUE;
		}
	}

	public double queryOneDayDelayAvg(AjaxDataQueryEntity entity) {
		Double[] values = queryGraphValue(entity, AjaxQueryType.DELAY);
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

	public Double[] queryGraphValue(AjaxDataQueryEntity entity, AjaxQueryType type) {
		List<AjaxData> datas = m_dataBuilder.queryByMinute(entity, type);
		DataSequence<AjaxData> ajaxDataSequence = buildAjaxDataSequence(datas, entity.getDate());

		switch (type) {
		case SUCCESS:
			return computeSuccessRatio(ajaxDataSequence);
		case REQUEST:
			return computeRequestCount(ajaxDataSequence);
		case DELAY:
			return computeDelayAvg(ajaxDataSequence);
		}

		return null;
	}

	public double[] queryAlertValue(AjaxDataQueryEntity entity, AjaxQueryType type) {
		List<AjaxData> datas = m_dataBuilder.queryByMinute(entity, type);
		int i = 0;

		switch (type) {
		case SUCCESS:
			Map<Integer, List<AjaxData>> dataMap = buildDataMap(datas);
			double[] successRatios = new double[dataMap.size()];

			for (Entry<Integer, List<AjaxData>> entry : dataMap.entrySet()) {
				successRatios[i] = computeSuccessRatio(entry.getValue());
				i++;
			}
			return successRatios;
		case REQUEST:
			double[] requestSum = new double[datas.size()];

			for (AjaxData data : datas) {
				requestSum[i] = data.getAccessNumberSum();
				i++;
			}
			return requestSum;
		case DELAY:
			double[] delay = new double[datas.size()];

			for (AjaxData data : datas) {
				long accessSumNum = data.getAccessNumberSum();

				if (accessSumNum > 0) {
					delay[i] = data.getResponseSumTimeSum() / accessSumNum;
				} else {
					delay[i] = 0.0;
				}
				i++;
			}
			return delay;
		}

		return null;
	}

	private void setFieldValue(AjaxDataDetail info, AjaxDataField field, int value) {
		switch (field) {
		case OPERATOR:
			info.setOperator(value);
			break;
		case CITY:
			info.setCity(value);
			break;
		case NETWORK:
			info.setNetwork(value);
			break;
		case CODE:
			break;
		}
	}

	private void updateAjaxDataDetailInfo(AjaxDataDetail info, Entry<Integer, List<AjaxData>> entry,
	      AjaxDataField field, AjaxDataQueryEntity entity) {
		int key = entry.getKey();
		List<AjaxData> datas = entry.getValue();
		long accessNumberSum = 0;
		long responseTimeSum = 0;
		long responsePackageSum = 0;
		long requestPackageSum = 0;

		for (AjaxData data : datas) {
			accessNumberSum += data.getAccessNumberSum();
			responseTimeSum += data.getResponseSumTimeSum();
			responsePackageSum += data.getResponseSumByteSum();
			requestPackageSum += data.getRequestSumByteSum();
		}

		double responseTimeAvg = accessNumberSum == 0 ? 0 : (double) responseTimeSum / accessNumberSum;
		double responsePackageAvg = accessNumberSum == 0 ? 0 : (double) responsePackageSum / accessNumberSum;
		double requestPackageAvg = accessNumberSum == 0 ? 0 : (double) requestPackageSum / accessNumberSum;

		info.setAccessNumberSum(accessNumberSum).setResponseTimeAvg(responseTimeAvg)
		      .setRequestPackageAvg(requestPackageAvg).setResponsePackageAvg(responsePackageAvg)
		      .setOperator(entity.getOperator()).setCity(entity.getCity()).setNetwork(entity.getNetwork());

		setFieldValue(info, field, key);
	}

}
