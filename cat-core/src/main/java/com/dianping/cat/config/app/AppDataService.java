package com.dianping.cat.config.app;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

	public void insert(Date period, int minute, int commandId, int city, int operator, int network, int appVersion,
	      int connectType, int code, int platform, int count, int responseSumTime, int requestPackage,
	      int responsePackage) throws DalException {
		AppDataCommand proto = new AppDataCommand();

		proto.setPeriod(period);
		proto.setMinuteOrder(minute);
		proto.setCommandId(commandId);
		proto.setCity(city);
		proto.setOperator(operator);
		proto.setNetwork(network);
		proto.setAppVersion(appVersion);
		proto.setConnnectType(connectType);
		proto.setCode(code);
		proto.setPlatform(platform);
		proto.setAccessNumber(count);
		proto.setResponseSumTime(responseSumTime);
		proto.setRequestPackage(requestPackage);
		proto.setResponsePackage(responsePackage);
		proto.setCreationDate(new Date());

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
			datas = m_dao.findData(commandId, period, city, operator, network, appVersion, connnectType, code, platform,
			      AppDataCommandEntity.READSET_DATA);

			Collections.sort(datas, new Comparator<AppDataCommand>() {
				@Override
				public int compare(AppDataCommand o1, AppDataCommand o2) {
					return (int) (o2.getMinuteOrder() - o1.getMinuteOrder());
				}
			});

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
		int i = 0;

		try {
			Map<Integer, List<AppDataCommand>> dataMap = convert2AppDataCommandMap(datas);
			int size = dataMap.size();

			if (size <= n) {
				for (Entry<Integer, List<AppDataCommand>> entry : dataMap.entrySet()) {
					long success = 0;
					long sum = 0;

					for (AppDataCommand data : entry.getValue()) {
						long number = data.getAccessNumberSum();

						if (isSuccessStatus(data)) {
							success += number;
						}
						sum += number;
					}
					value[i++] = (double) success / sum;
				}
			} else {
				Cat.logError(new RuntimeException("query database minute number " + size + " lagger than expected size "
				      + n));
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
		int i = 0;

		for (AppDataCommand data : datas) {
			long count = data.getAccessNumberSum();

			if (i < n) {
				value[i++] = count;
			}
		}
		values.put(DELAY_AVG, value);
		return values;
	}

	public Map<String, double[]> queryDelayAvg(List<AppDataCommand> datas, int n) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		double[] value = new double[n];
		int i = 0;

		for (AppDataCommand data : datas) {
			long count = data.getAccessNumberSum();
			long sum = data.getResponseSumTimeSum();

			double avg = sum / count;
			if (i < n) {
				value[i++] = avg;
			}
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
