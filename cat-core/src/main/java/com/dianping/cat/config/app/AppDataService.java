package com.dianping.cat.config.app;

import java.util.Collection;
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
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.app.AppDataCommandEntity;
import com.dianping.cat.configuration.app.entity.Code;

public class AppDataService {

	@Inject
	private AppDataCommandDao m_dao;

	@Inject
	private AppConfigManager m_appConfigManager;

	public static final String SUCCESS_RATIO = "successRatio";

	public static final String REQUEST_COUNT = "requestCount";

	public static final String DELAY_AVG = "delayAvg";

	private static final int MAX_SIZE = 288;

	public void insert(AppDataCommand proto) throws DalException {
		m_dao.insertData(proto);
	}

	public Map<String, double[]> queryValue(QueryEntity entity, String type) {
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
			datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
			      platform, AppDataCommandEntity.READSET_COUNT_DATA);
			int n = calculateSize(entity.getDate().getTime());

			if (SUCCESS_RATIO.equals(type)) {
				return querySuccessRatio(datas, n);
			} else if (REQUEST_COUNT.equals(type)) {
				return queryRequestCount(datas, n);
			} else if (DELAY_AVG.equals(type)) {
				return queryDelayAvg(datas, n);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return new LinkedHashMap<String, double[]>();
	}

	private Map<Integer, List<AppDataCommand>> convert2AppDataCommandMap(List<AppDataCommand> fromDatas) {
		Map<Integer, List<AppDataCommand>> dataMap = new LinkedHashMap<Integer, List<AppDataCommand>>();

		for (AppDataCommand from : fromDatas) {
			int minute = from.getMinuteOrder();
			List<AppDataCommand> data = dataMap.get(minute);

			if (data == null) {
				data = new LinkedList<AppDataCommand>();

				dataMap.put(minute, data);
			}
			data.add(from);
		}
		return dataMap;
	}

	public Map<String, double[]> querySuccessRatio(List<AppDataCommand> datas, int n) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		double[] value = new double[n];

		try {
			Map<Integer, List<AppDataCommand>> dataMap = convert2AppDataCommandMap(datas);

			for (Entry<Integer, List<AppDataCommand>> entry : dataMap.entrySet()) {
				int key = entry.getKey();
				long success = 0;
				long sum = 0;

				for (AppDataCommand data : entry.getValue()) {
					long number = data.getAccessNumberSum();

					if (isSuccessStatus(data)) {
						success += number;
					}
					sum += number;
				}
				value[key / 5] = (double) success / sum;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		values.put(DELAY_AVG, value);
		return values;
	}

	private boolean isSuccessStatus(AppDataCommand data) {
		int code = data.getCode();
		Collection<Code> codes = m_appConfigManager.queryCodeByCommand(data.getCommandId());

		for (Code c : codes) {
			if (c.getId() == code) {
				return (c.getStatus() == 0);
			}
		}
		return false;
	}

	public Map<String, double[]> queryRequestCount(List<AppDataCommand> datas, int n) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		double[] value = new double[n];

		for (AppDataCommand data : datas) {
			long count = data.getAccessNumberSum();

			value[data.getMinuteOrder() / 5] = count;
		}
		values.put(DELAY_AVG, value);
		return values;
	}

	public Map<String, double[]> queryDelayAvg(List<AppDataCommand> datas, int n) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		double[] value = new double[n];

		for (AppDataCommand data : datas) {
			long count = data.getAccessNumberSum();
			long sum = data.getResponseSumTimeSum();

			double avg = sum / count;
			value[data.getMinuteOrder() / 5] = avg;
		}
		values.put(DELAY_AVG, value);
		return values;
	}

	private int calculateSize(long startTime) {
		int n = MAX_SIZE;
		int oneDay = 24 * 3600 * 1000;

		if (startTime + oneDay > System.currentTimeMillis()) {
			long current = System.currentTimeMillis();
			long endTime = current - current % 300000;
			
			n = (int) (endTime - startTime) / 300000;
		}
		return n;
	}
}
