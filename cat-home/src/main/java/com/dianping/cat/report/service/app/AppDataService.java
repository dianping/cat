package com.dianping.cat.report.service.app;

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
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.app.AppDataCommandEntity;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppDataSequence;

public class AppDataService {

	@Inject
	private AppDataCommandDao m_dao;

	@Inject
	private AppConfigManager m_appConfigManager;

	public static final String SUCCESS = "success";

	public static final String REQUEST = "request";

	public static final String DELAY = "delay";

	public static final String REQUEST_PACKAGE = "requestPackage";

	public static final String RESPONSE_PACKAGE = "responsePackage";

	public List<AppDataDetail> buildAppDataDetailInfos(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppDataDetail> infos = new LinkedList<AppDataDetail>();
		List<AppDataCommand> datas = queryByFieldCode(entity, groupByField);
		Map<Integer, List<AppDataCommand>> field2Datas = buildFields2Datas(datas, groupByField);

		for (Entry<Integer, List<AppDataCommand>> entry : field2Datas.entrySet()) {
			List<AppDataCommand> datalst = entry.getValue();
			AppDataDetail info = new AppDataDetail();
			double ratio = computeSuccessRatio(entity.getId(), datalst);

			info.setSuccessRatio(ratio);
			updateAppDataDetailInfo(info, entry, groupByField, entity);
			infos.add(info);
		}
		return infos;
	}

	private Map<Integer, List<AppDataCommand>> buildFields2Datas(List<AppDataCommand> datas, AppDataField field) {
		Map<Integer, List<AppDataCommand>> field2Datas = new HashMap<Integer, List<AppDataCommand>>();

		for (AppDataCommand data : datas) {
			int fieldValue = queryFieldValue(data, field);
			List<AppDataCommand> lst = field2Datas.get(fieldValue);

			if (lst == null) {
				lst = new ArrayList<AppDataCommand>();
				field2Datas.put(fieldValue, lst);
			}
			lst.add(data);
		}
		return field2Datas;
	}

	public Double[] computeDelayAvg(AppDataSequence<AppDataCommand> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getRecords().entrySet()) {
			for (AppDataCommand data : entry.getValue()) {
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

	public Double[] computeRequestCount(AppDataSequence<AppDataCommand> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getRecords().entrySet()) {
			for (AppDataCommand data : entry.getValue()) {
				double count = data.getAccessNumberSum();
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = count;
				}
			}
		}
		return value;
	}

	public Double[] computeSuccessRatio(int commandId, AppDataSequence<AppDataCommand> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (int i = 0; i < n; i++) {
			value[i] = 100.0;
		}

		try {
			for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getRecords().entrySet()) {
				int key = entry.getKey();
				int index = key / 5;

				if (index < n) {
					value[index] = computeSuccessRatio(commandId, entry.getValue());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return value;
	}

	private double computeSuccessRatio(int commandId, List<AppDataCommand> datas) {
		long success = 0;
		long sum = 0;

		for (AppDataCommand data : datas) {
			long number = data.getAccessNumberSum();

			if (m_appConfigManager.isSuccessCode(commandId, data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private AppDataSequence<AppDataCommand> buildAppSequence(List<AppDataCommand> fromDatas, Date period) {
		Map<Integer, List<AppDataCommand>> dataMap = new LinkedHashMap<Integer, List<AppDataCommand>>();
		int max = -5;

		for (AppDataCommand from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
			List<AppDataCommand> data = dataMap.get(minute);

			if (data == null) {
				data = new LinkedList<AppDataCommand>();

				dataMap.put(minute, data);
			}
			data.add(from);
		}
		int n = max / 5 + 1;
		int length = queryAppDataDuration(period, n);

		return new AppDataSequence<AppDataCommand>(length, dataMap);
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

	private int queryFieldValue(AppDataCommand data, AppDataField field) {
		switch (field) {
		case OPERATOR:
			return data.getOperator();
		case APP_VERSION:
			return data.getAppVersion();
		case CITY:
			return data.getCity();
		case CONNECT_TYPE:
			return data.getConnnectType();
		case NETWORK:
			return data.getNetwork();
		case PLATFORM:
			return data.getPlatform();
		case CODE:
		default:
			return CommandQueryEntity.DEFAULT_VALUE;
		}
	}

	public double queryOneDayDelayAvg(CommandQueryEntity entity) {
		Double[] values = queryValue(entity, AppDataService.DELAY);
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
		case CODE:
			break;
		}
	}

	private void updateAppDataDetailInfo(AppDataDetail info, Entry<Integer, List<AppDataCommand>> entry,
	      AppDataField field, CommandQueryEntity entity) {
		int key = entry.getKey();
		List<AppDataCommand> datas = entry.getValue();
		long accessNumberSum = 0;
		long responseTimeSum = 0;
		long responsePackageSum = 0;
		long requestPackageSum = 0;

		for (AppDataCommand data : datas) {
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
		      .setConnectType(entity.getConnectType());

		setFieldValue(info, field, key);
	}

	public List<AppDataCommand> queryByField(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppDataCommand> datas = new ArrayList<AppDataCommand>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperator(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_OPERATOR_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetwork(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_NETWORK_DATA);
				break;
			case APP_VERSION:
				datas = m_dao.findDataByAppVersion(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_APP_VERSION_DATA);
				break;
			case CONNECT_TYPE:
				datas = m_dao.findDataByConnnectType(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_CONNNECT_TYPE_DATA);
				break;
			case PLATFORM:
				datas = m_dao.findDataByPlatform(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_PLATFORM_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCity(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_CITY_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_CODE_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public List<AppDataCommand> queryByFieldCode(CommandQueryEntity entity, AppDataField groupByField) {
		List<AppDataCommand> datas = new ArrayList<AppDataCommand>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_OPERATOR_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_NETWORK_CODE_DATA);
				break;
			case APP_VERSION:
				datas = m_dao.findDataByAppVersionCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, AppDataCommandEntity.READSET_APP_VERSION_CODE__DATA);
				break;
			case CONNECT_TYPE:
				datas = m_dao.findDataByConnnectTypeCode(commandId, period, city, operator, network, appVersion,
				      connnectType, code, platform, AppDataCommandEntity.READSET_CONNECT_TYPE_CODE_DATA);
				break;
			case PLATFORM:
				datas = m_dao.findDataByPlatformCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_PLATFORM_CODE_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCityCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_CITY_CODE_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_CODE_DATA);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public Double[] queryValue(CommandQueryEntity entity, String type) {
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		List<AppDataCommand> datas = new ArrayList<AppDataCommand>();

		try {
			if (SUCCESS.equals(type)) {
				datas = m_dao.findDataByMinuteCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_SUCCESS_DATA);
				AppDataSequence<AppDataCommand> s = buildAppSequence(datas, entity.getDate());

				return computeSuccessRatio(commandId, s);
			} else if (REQUEST.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_COUNT_DATA);
				AppDataSequence<AppDataCommand> s = buildAppSequence(datas, entity.getDate());

				return computeRequestCount(s);
			} else if (DELAY.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_AVG_DATA);
				AppDataSequence<AppDataCommand> s = buildAppSequence(datas, entity.getDate());

				return computeDelayAvg(s);
			} else {
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

}
