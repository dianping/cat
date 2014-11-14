package com.dianping.cat.service.app.speed;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.AppSpeedDataEntity;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.service.app.BaseAppDataService;
import com.dianping.cat.service.app.BaseQueryEntity;

public class AppSpeedDataService implements BaseAppDataService<AppSpeedData> {

	@Inject
	private AppSpeedDataDao m_dao;

	@Inject
	private AppSpeedConfigManager m_configManager;

	public static final String SLOW_RATIO = "slowRatio";

	public static final String REQUEST = "request";

	public static final String DELAY = "delay";

	public static final String ID = AppSpeedData.class.getName();

	public Double[] computeDelayAvg(AppSpeedDataMap convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppSpeedData>> entry : convertedData.getAppSpeedDatas().entrySet()) {
			for (AppSpeedData data : entry.getValue()) {
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

	public Double[] computeRequestCount(AppSpeedDataMap convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AppSpeedData>> entry : convertedData.getAppSpeedDatas().entrySet()) {
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

	private AppSpeedDataMap convert2AppDataCommandMap(List<AppSpeedData> fromDatas, Date period) {
		Map<Integer, List<AppSpeedData>> dataMap = new LinkedHashMap<Integer, List<AppSpeedData>>();
		int max = -1;

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

		return new AppSpeedDataMap(length, dataMap);
	}

	@Override
	public int[] insert(AppSpeedData[] proto) throws DalException {
		return m_dao.insert(proto);
	}

	@Override
	public void insertSingle(AppSpeedData proto) throws DalException {
		m_dao.insert(proto);
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

	private int queryFieldValue(AppDataCommand data, AppSpeedDataField field) {
		switch (field) {
		case OPERATOR:
			return data.getOperator();
		case APP_VERSION:
			return data.getAppVersion();
		case CITY:
			return data.getCity();
		case NETWORK:
			return data.getNetwork();
		case PLATFORM:
			return data.getPlatform();
		default:
			return BaseQueryEntity.DEFAULT_VALUE;
		}
	}

	public double queryOneDayDelayAvg(BaseQueryEntity entity) {
		AppSpeedDataMap map = queryValue(entity);
		Double[] values = computeDelayAvg(map);
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

	public AppSpeedDataMap queryValue(BaseQueryEntity entity) {
		int speedId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int platform = entity.getPlatfrom();
		List<AppSpeedData> datas;

		try {
			datas = m_dao.findDataByMinute(speedId, period, city, operator, network, appVersion, platform,
			      AppSpeedDataEntity.READSET_COUNT_DATA);

			return convert2AppDataCommandMap(datas, period);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	public class AppSpeedDataMap {
		private int m_duration;

		private Map<Integer, List<AppSpeedData>> m_appSpeedDatas;

		public AppSpeedDataMap(int duration, Map<Integer, List<AppSpeedData>> appSpeedDatas) {
			m_duration = duration;
			m_appSpeedDatas = appSpeedDatas;
		}

		public Map<Integer, List<AppSpeedData>> getAppSpeedDatas() {
			return m_appSpeedDatas;
		}

		public int getDuration() {
			return m_duration;
		}
	}

}
