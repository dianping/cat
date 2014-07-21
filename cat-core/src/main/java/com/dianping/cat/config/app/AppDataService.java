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
import org.unidal.tuple.Pair;

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

	public static final String SUCCESS= "success";

	public static final String REQUEST = "request";

	public static final String DELAY = "delay";

	public void insert(AppDataCommand proto) throws DalException {
		m_dao.insert(proto);
	}

	public void insert(AppDataCommand[] proto) throws DalException {
		m_dao.insert(proto);
	}
	
	public double[] queryValue(QueryEntity entity, String type) {
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
				Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair = convert2AppDataCommandMap(datas);

				return querySuccessRatio(commandId, dataPair);
			} else if (REQUEST.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_COUNT_DATA);

				Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair = convert2AppDataCommandMap(datas);
				return queryRequestCount(dataPair);
			} else if (DELAY.equals(type)) {
				datas = m_dao.findDataByMinute(commandId, period, city, operator, network, appVersion, connnectType, code,
				      platform, AppDataCommandEntity.READSET_AVG_DATA);

				Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair = convert2AppDataCommandMap(datas);
				return queryDelayAvg(dataPair);
			}

		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}
		return null;
	}

	private Pair<Integer, Map<Integer, List<AppDataCommand>>> convert2AppDataCommandMap(List<AppDataCommand> fromDatas) {
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
<<<<<<< HEAD
		int gap = max - min;
		int n = gap <= 0 ? max / 5 : gap / 5;
=======

		int n = max / 5;
>>>>>>> 679260357982378d8ec37b01091bbd37da70b3db

		return new Pair<Integer, Map<Integer, List<AppDataCommand>>>(n, dataMap);
	}

	public double[] querySuccessRatio(int commandId, Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair) {
		double[] value = new double[dataPair.getKey()];
		Map<Integer, List<AppDataCommand>> dataMap = dataPair.getValue();

		try {
			for (Entry<Integer, List<AppDataCommand>> entry : dataMap.entrySet()) {
				int key = entry.getKey();
				long success = 0;
				long sum = 0;

				for (AppDataCommand data : entry.getValue()) {
					long number = data.getAccessNumberSum();

					if (isSuccessStatus(commandId, data.getCode())) {
						success += number;
					}
					sum += number;
				}
				value[key / 5 - 1] = (double) success / sum;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return value;
	}

<<<<<<< HEAD
	private boolean isSuccessStatus(AppDataCommand data) {
		int code = data.getCode();
		Collection<Code> codes = m_appConfigManager.queryCodeByCommand(data.getCommandId());
=======
	private boolean isSuccessStatus(int commandId, int code) {
		Collection<Code> codes = m_appConfigManager.queryCodeByCommand(commandId);

>>>>>>> 679260357982378d8ec37b01091bbd37da70b3db
		for (Code c : codes) {
			if (c.getId() == code) {
				return (c.getStatus() == 0);
			}
		}
		return false;
	}

	public double[] queryRequestCount(Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair) {
		double[] value = new double[dataPair.getKey()];

		for (Entry<Integer, List<AppDataCommand>> entry : dataPair.getValue().entrySet()) {
			for (AppDataCommand data : entry.getValue()) {
				long count = data.getAccessNumberSum();

				value[data.getMinuteOrder() / 5 - 1] = count;
			}
		}
		return value;
	}

	public double[] queryDelayAvg(Pair<Integer, Map<Integer, List<AppDataCommand>>> dataPair) {
		double[] value = new double[dataPair.getKey()];

		for (Entry<Integer, List<AppDataCommand>> entry : dataPair.getValue().entrySet()) {
			for (AppDataCommand data : entry.getValue()) {
				long count = data.getAccessNumberSum();
				long sum = data.getResponseSumTimeSum();

				double avg = sum / count;
				value[data.getMinuteOrder() / 5 - 1] = avg;
			}
		}
		return value;
	}
}
