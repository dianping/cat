package com.dianping.cat.report.page.app.service;

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
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppCommandDataEntity;
import com.dianping.cat.app.AppDataField;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.report.page.DataSequence;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.display.AppDataDetail;

@Named
public class AppDataService {

	@Inject
	private AppCommandDataDao m_dao;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	public List<AppDataDetail> buildAppDataDetailInfos(CommandQueryEntity entity, AppDataField groupByField,
	      QueryType type) {
		List<AppDataDetail> infos = new LinkedList<AppDataDetail>();
		List<AppCommandData> datas = queryByFieldCode(entity, groupByField);
		Map<Integer, List<AppCommandData>> field2Datas = buildFields2Datas(datas, groupByField);

		for (Entry<Integer, List<AppCommandData>> entry : field2Datas.entrySet()) {
			List<AppCommandData> datalst = entry.getValue();
			AppDataDetail info = new AppDataDetail();
			double ratio = computeSuccessRatio(entity.getId(), datalst, type);

			info.setSuccessRatio(ratio);
			updateAppDataDetailInfo(info, entry, groupByField, entity);
			infos.add(info);
		}
		return infos;
	}

	public Map<Integer, List<AppCommandData>> buildDataMap(List<AppCommandData> datas) {
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

	private DataSequence<AppCommandData> buildAppSequence(List<AppCommandData> fromDatas, Date period) {
		Map<Integer, List<AppCommandData>> dataMap = buildDataMap(fromDatas);
		int max = -5;

		for (AppCommandData from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
		}
		int n = max / 5 + 1;
		int length = queryAppDataDuration(period, n);

		return new DataSequence<AppCommandData>(length, dataMap);
	}

	private Map<Integer, List<AppCommandData>> buildFields2Datas(List<AppCommandData> datas, AppDataField field) {
		Map<Integer, List<AppCommandData>> field2Datas = new HashMap<Integer, List<AppCommandData>>();

		for (AppCommandData data : datas) {
			int fieldValue = queryFieldValue(data, field);
			List<AppCommandData> lst = field2Datas.get(fieldValue);

			if (lst == null) {
				lst = new ArrayList<AppCommandData>();
				field2Datas.put(fieldValue, lst);
			}
			lst.add(data);
		}
		return field2Datas;
	}

	public Double[] computeDelayAvg(DataSequence<AppCommandData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppCommandData>> entry : convertedData.getRecords().entrySet()) {
			for (AppCommandData data : entry.getValue()) {
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

	public Double[] computeRequestCount(DataSequence<AppCommandData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppCommandData>> entry : convertedData.getRecords().entrySet()) {
			for (AppCommandData data : entry.getValue()) {
				double count = data.getAccessNumberSum();
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = count;
				}
			}
		}
		return value;
	}

	public Double[] computeSuccessRatio(int commandId, DataSequence<AppCommandData> convertedData, QueryType type) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (int i = 0; i < n; i++) {
			value[i] = 100.0;
		}

		try {
			for (Entry<Integer, List<AppCommandData>> entry : convertedData.getRecords().entrySet()) {
				int key = entry.getKey();
				int index = key / 5;

				if (index < n) {
					value[index] = computeSuccessRatio(commandId, entry.getValue(), type);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return value;
	}

	public double computeSuccessRatio(int commandId, List<AppCommandData> datas, QueryType type) {
		long success = 0;
		long sum = 0;

		for (AppCommandData data : datas) {
			long number = data.getAccessNumberSum();

			switch (type) {
			case REQUEST:
			case NETWORK_SUCCESS:
			case DELAY:
				if (m_appConfigManager.isSuccessCode(commandId, data.getCode())) {
					success += number;
				}
				break;
			case BUSINESS_SUCCESS:
				if (m_appConfigManager.isBusinessSuccessCode(commandId, data.getCode())) {
					success += number;
				}
				break;
			default:
				throw new RuntimeException("unexpected query type, type:" + type);
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
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

	public List<AppCommandData> queryByField(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppCommandData> datas = new ArrayList<AppCommandData>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int source = entity.getSource();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperator(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_OPERATOR_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetwork(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_NETWORK_DATA);
				break;
			case APP_VERSION:
				datas = m_dao.findDataByAppVersion(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_APP_VERSION_DATA);
				break;
			case CONNECT_TYPE:
				datas = m_dao.findDataByConnectType(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_CONNECT_TYPE_DATA);
				break;
			case PLATFORM:
				datas = m_dao.findDataByPlatform(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_PLATFORM_DATA);
				break;
			case SOURCE:
				datas = m_dao.findDataBySource(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_SOURCE_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCity(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CITY_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CODE_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public List<AppCommandData> queryByFieldCode(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppCommandData> datas = new ArrayList<AppCommandData>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int source = entity.getSource();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_OPERATOR_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_NETWORK_CODE_DATA);
				break;
			case APP_VERSION:
				datas = m_dao.findDataByAppVersionCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_APP_VERSION_CODE_DATA);
				break;
			case CONNECT_TYPE:
				datas = m_dao.findDataByConnectTypeCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_CONNECT_TYPE_CODE_DATA);
				break;
			case PLATFORM:
				datas = m_dao.findDataByPlatformCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_PLATFORM_CODE_DATA);
				break;
			case SOURCE:
				datas = m_dao.findDataBySourceCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, startMinuteOrder, endMinuteOrder,
				      AppCommandDataEntity.READSET_SOURCE_CODE_DATA);
				break;
			case CITY:
				datas = m_dao
				      .findDataByCityCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				            platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CITY_CODE_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, startMinuteOrder, endMinuteOrder, AppCommandDataEntity.READSET_CODE_DATA);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public int queryFieldValue(AppCommandData data, AppDataField field) {
		switch (field) {
		case OPERATOR:
			return data.getOperator();
		case APP_VERSION:
			return data.getAppVersion();
		case CITY:
			return data.getCity();
		case CONNECT_TYPE:
			return data.getConnectType();
		case NETWORK:
			return data.getNetwork();
		case PLATFORM:
			return data.getPlatform();
		case SOURCE:
			return data.getSource();
		case CODE:
		default:
			return CommandQueryEntity.DEFAULT_VALUE;
		}
	}

	public double queryOneDayDelayAvg(CommandQueryEntity entity) {
		Double[] values = queryGraphValue(entity, QueryType.DELAY);
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

	public List<AppCommandData> queryByMinute(CommandQueryEntity entity, QueryType type) {
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int source = entity.getSource();
		int start = entity.getStartMinuteOrder();
		int end = entity.getEndMinuteOrder();
		List<AppCommandData> datas = new ArrayList<AppCommandData>();

		try {
			switch (type) {
			case NETWORK_SUCCESS:
			case BUSINESS_SUCCESS:
				datas = m_dao.findDataByMinuteCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, source, start, end, AppCommandDataEntity.READSET_SUCCESS_DATA);
				break;
			case REQUEST:
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, start, end, AppCommandDataEntity.READSET_COUNT_DATA);
				break;
			case DELAY:
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, source, start, end, AppCommandDataEntity.READSET_AVG_DATA);
				break;
			default:
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public Double[] queryGraphValue(CommandQueryEntity entity, QueryType type) {
		List<AppCommandData> datas = queryByMinute(entity, type);
		DataSequence<AppCommandData> s = buildAppSequence(datas, entity.getDate());

		switch (type) {
		case NETWORK_SUCCESS:
		case BUSINESS_SUCCESS:
			return computeSuccessRatio(entity.getId(), s, type);
		case REQUEST:
			return computeRequestCount(s);
		case DELAY:
			return computeDelayAvg(s);
		default:
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	public double[] queryAlertValue(CommandQueryEntity entity, QueryType type, int minutes) {
		List<AppCommandData> datas = queryByMinute(entity, type);
		int i = 0;

		switch (type) {
		case NETWORK_SUCCESS:
			Map<Integer, List<AppCommandData>> dataMap = buildDataMap(datas);
			double[] successRatios = new double[dataMap.size()];

			for (Entry<Integer, List<AppCommandData>> entry : dataMap.entrySet()) {
				successRatios[i] = computeSuccessRatio(entity.getId(), entry.getValue(), type);
				i++;
			}
			return successRatios;
		case REQUEST:
			double[] requestSum = new double[minutes / 5];

			for (AppCommandData data : datas) {
				requestSum[i] = data.getAccessNumberSum();
				i++;
			}
			return requestSum;
		case DELAY:
			double[] delay = new double[datas.size()];

			for (AppCommandData data : datas) {
				long accessSumNum = data.getAccessNumberSum();

				if (accessSumNum > 0) {
					delay[i] = data.getResponseSumTimeSum() / accessSumNum;
				} else {
					delay[i] = 0.0;
				}
				i++;
			}
			return delay;
		default:
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	private void setFieldValue(AppDataDetail info, AppDataField field, int value) {
		switch (field) {
		case OPERATOR:
			info.setOperator(value);
			break;
		case APP_VERSION:
			info.setAppVersion(value);
			break;
		case CITY:
			info.setCity(value);
			break;
		case CONNECT_TYPE:
			info.setConnectType(value);
			break;
		case NETWORK:
			info.setNetwork(value);
			break;
		case PLATFORM:
			info.setPlatform(value);
			break;
		case SOURCE:
			info.setSource(value);
			break;
		case CODE:
			break;
		}
	}

	private void updateAppDataDetailInfo(AppDataDetail info, Entry<Integer, List<AppCommandData>> entry,
	      AppDataField field, CommandQueryEntity entity) {
		int key = entry.getKey();
		List<AppCommandData> datas = entry.getValue();
		long accessNumberSum = 0;
		long responseTimeSum = 0;
		long responsePackageSum = 0;
		long requestPackageSum = 0;

		for (AppCommandData data : datas) {
			accessNumberSum += data.getAccessNumberSum();
			responseTimeSum += data.getResponseSumTimeSum();
			responsePackageSum += data.getResponsePackageSum();
			requestPackageSum += data.getRequestPackageSum();
		}
		double responseTimeAvg = accessNumberSum == 0 ? 0 : (double) responseTimeSum / accessNumberSum;
		double responsePackageAvg = accessNumberSum == 0 ? 0 : (double) responsePackageSum / accessNumberSum;
		double requestPackageAvg = accessNumberSum == 0 ? 0 : (double) requestPackageSum / accessNumberSum;

		info.setAccessNumberSum(accessNumberSum).setResponseTimeAvg(responseTimeAvg)
		      .setRequestPackageAvg(requestPackageAvg).setResponsePackageAvg(responsePackageAvg)
		      .setOperator(entity.getOperator()).setCity(entity.getCity()).setNetwork(entity.getNetwork())
		      .setAppVersion(entity.getVersion()).setPlatform(entity.getPlatfrom())
		      .setConnectType(entity.getConnectType()).setSource(entity.getSource());

		setFieldValue(info, field, key);
	}

}
