package com.dianping.cat.config.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.app.AppDataCommandEntity;
import com.dianping.cat.configuration.app.entity.Code;

public class AppDataService {

	@Inject
	private AppDataCommandDao m_dao;

	@Inject
	private AppConfigManager m_appConfigManager;

	public static final String SUCCESS = "success";

	public static final String REQUEST = "request";

	public static final String DELAY = "delay";

	public void insertSignal(AppDataCommand proto) throws DalException {
		m_dao.insert(proto);
	}

	public void insert(AppDataCommand[] proto) throws DalException {
		m_dao.insert(proto);
	}

	public Double[] queryValue(QueryEntity entity, String type) {
		int commandId = entity.getCommand();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getChannel();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		List<AppDataCommand> datas;

		try {
			if (SUCCESS.equals(type)) {
				datas = m_dao.findDataByMinuteCode(commandId, period, city, operator, network, appVersion, connnectType,
				      code, platform, AppDataCommandEntity.READSET_SUCCESS_DATA);
				AppDataCommandMap convertedData = convert2AppDataCommandMap(datas, period);

				return querySuccessRatio(commandId, convertedData);
			} else if (REQUEST.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_COUNT_DATA);
				AppDataCommandMap convertedData = convert2AppDataCommandMap(datas, period);

				return queryRequestCount(convertedData);
			} else if (DELAY.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_AVG_DATA);
				AppDataCommandMap dataPair = convert2AppDataCommandMap(datas, period);

				return queryDelayAvg(dataPair);
			} else {
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	private int queryTenMinutesBackLength(Date period, int n) {
		int size = n;
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (period.equals(cal.getTime())) {
			long start = cal.getTimeInMillis();
			long current = System.currentTimeMillis();
			int length = (int) (current - current % 300000 - start) / 300000 - 1;
			size = length < 0 ? 0 : length;
		}
		return size;
	}

	private AppDataCommandMap convert2AppDataCommandMap(List<AppDataCommand> fromDatas, Date period) {
		Map<Integer, List<AppDataCommand>> dataMap = new LinkedHashMap<Integer, List<AppDataCommand>>();
		int max = -1;

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
		int length = queryTenMinutesBackLength(period, n);

		return new AppDataCommandMap(length, dataMap);
	}

	public Double[] querySuccessRatio(int commandId, AppDataCommandMap convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (int i = 0; i < n; i++) {
			value[i] = 100.0;
		}

		try {
			for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getAppDataCommands().entrySet()) {
				int key = entry.getKey();
				int index = key / 5;

				if (index < n) {
					value[index] = querySuccessRatio(commandId, entry.getValue());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return value;
	}

	public Double[] queryRequestCount(AppDataCommandMap convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getAppDataCommands().entrySet()) {
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

	public Double[] queryDelayAvg(AppDataCommandMap convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppDataCommand>> entry : convertedData.getAppDataCommands().entrySet()) {
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

	public double queryOneDayDelayAvg(QueryEntity entity) {
		Double[] values = queryValue(entity, DELAY);
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

	private int queryFieldValue(AppDataCommand data, AppDataGroupByField field) {
		if (field != null) {
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
			}
		}
		return -1;
	}

	private void setFieldValue(AppDataSpreadInfo info, AppDataGroupByField field, int value) {
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
		}
	}

	private Map<Integer, List<AppDataCommand>> buildFields2Datas(List<AppDataCommand> datas, AppDataGroupByField field) {
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

	private boolean isSuccessStatus(int commandId, int code) {
		Collection<Code> codes = m_appConfigManager.queryCodeByCommand(commandId);

		for (Code c : codes) {
			if (c.getId() == code) {
				return (c.getStatus() == 0);
			}
		}
		return false;
	}

	private double querySuccessRatio(int commandId, List<AppDataCommand> datas) {
		long success = 0;
		long sum = 0;

		for (AppDataCommand data : datas) {
			long number = data.getAccessNumberSum();

			if (isSuccessStatus(commandId, data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private void updateAppDataSpreadInfo(AppDataSpreadInfo info, Entry<Integer, List<AppDataCommand>> entry,
	      AppDataGroupByField field, QueryEntity entity) {
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

		info.setAccessNumberSum(accessNumberSum).setResponseTimeAvg(convertDouble(responseTimeAvg))
		      .setRequestPackageAvg(convertDouble(requestPackageAvg))
		      .setResponsePackageAvg(convertDouble(responsePackageAvg)).setOperator(entity.getOperator())
		      .setCity(entity.getCity()).setNetwork(entity.getNetwork()).setAppVersion(entity.getVersion())
		      .setPlatform(entity.getPlatfrom()).setConnectType(entity.getChannel());

		if (field != null) {
			setFieldValue(info, field, key);
		}
	}

	private double convertDouble(double data) {
		return (int) (data * 100) / 100.0;
	}

	public List<AppDataSpreadInfo> buildAppDataSpreadInfo(QueryEntity entity, AppDataGroupByField groupByField) {
		List<AppDataSpreadInfo> infos = new ArrayList<AppDataSpreadInfo>();
		List<AppDataCommand> datas = queryAppDataCommands(entity, groupByField);
		Map<Integer, List<AppDataCommand>> field2Datas = buildFields2Datas(datas, groupByField);

		for (Entry<Integer, List<AppDataCommand>> entry : field2Datas.entrySet()) {
			List<AppDataCommand> datalst = entry.getValue();
			AppDataSpreadInfo info = new AppDataSpreadInfo();
			double ratio = querySuccessRatio(entity.getCommand(), datalst);

			info.setSuccessRatio(convertDouble(ratio));
			updateAppDataSpreadInfo(info, entry, groupByField, entity);
			infos.add(info);
		}
		
		return infos;
	}

	private List<AppDataCommand> queryAppDataCommands(QueryEntity entity, AppDataGroupByField groupByField) {
		List<AppDataCommand> datas = new ArrayList<AppDataCommand>();
		int commandId = entity.getCommand();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getChannel();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			if (groupByField != null) {
				switch (groupByField) {
				case OPERATOR:
					datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, appVersion,
					      connnectType, code, platform, AppDataCommandEntity.READSET_OPERATOR_CODE_DATA);
					break;
				case NETWORK:
					datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, appVersion,
					      connnectType, code, platform, AppDataCommandEntity.READSET_NETWORK_CODE_DATA);
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
					datas = m_dao.findDataByPlatformCode(commandId, period, city, operator, network, appVersion,
					      connnectType, code, platform, AppDataCommandEntity.READSET_PLATFORM_CODE_DATA);
					break;
				case CITY:
					datas = m_dao.findDataByCityCode(commandId, period, city, operator, network, appVersion, connnectType,
					      code, platform, AppDataCommandEntity.READSET_CITY_CODE_DATA);
					break;
				}
			} else {
				datas = m_dao.findDataByCode(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, startMinuteOrder, endMinuteOrder, AppDataCommandEntity.READSET_CODE_DATA);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public class AppDataCommandMap {
		private int m_duration;

		private Map<Integer, List<AppDataCommand>> m_appDataCommands;

		public int getDuration() {
			return m_duration;
		}

		public Map<Integer, List<AppDataCommand>> getAppDataCommands() {
			return m_appDataCommands;
		}

		public AppDataCommandMap(int duration, Map<Integer, List<AppDataCommand>> appDataCommands) {
			m_duration = duration;
			m_appDataCommands = appDataCommands;
		}
	}
}
