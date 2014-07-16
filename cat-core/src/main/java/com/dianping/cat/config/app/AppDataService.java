package com.dianping.cat.config.app;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.app.AppDataCommandEntity;

public class AppDataService {

	@Inject
	private AppDataCommandDao m_dao;

	public static final String SUCCESS_RATIO = "successRatio";

	public static final String REQUEST_COUNT = "requestCount";

	public static final String DELAY_AVG = "delayAvg";

	public void insert() {

	}

	public Map<String, double[]> queryAppValue(QueryEntity entity, String type) {
		if (SUCCESS_RATIO.equals(type)) {
			return querySuccessRatio(entity);
		} else if (REQUEST_COUNT.equals(type)) {
			return queryRequestCount(entity);
		} else if (DELAY_AVG.equals(type)) {
			return queryDelayAvg(entity);
		} else {
			return new LinkedHashMap<String, double[]>();
		}
	}

	private Map<String, double[]> makeMockValue(String type) {
		Map<String, double[]> map = new LinkedHashMap<String, double[]>();
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		long startTime = cal.getTime().getTime();
		long current = System.currentTimeMillis();
		long endTime = current - current % 300000;
		int n = (int) (endTime - startTime) / 300000;
		double[] value = new double[n];

		for (int i = 0; i < n; i++) {
			value[i] = (new Random().nextDouble() + 1) * 100;
		}
		map.put(type, value);
		return map;
	}

	private Map<String, double[]> querySuccessRatio(QueryEntity entity) {

		return makeMockValue(SUCCESS_RATIO);
	}

	private Map<String, double[]> queryDelayAvg(QueryEntity entity) {

		return makeMockValue(DELAY_AVG);
	}

	private Map<String, double[]> queryRequestCount(QueryEntity entity) {

		return makeMockValue(REQUEST_COUNT);
	}

	public void queryAvg(QueryEntity entity) {
		int commandId = entity.getCommand();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getChannel();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();

		try {
			List<AppDataCommand> datas = m_dao.findData(commandId, period, city, operator, network, appVersion,
			      connnectType, code, platform, AppDataCommandEntity.READSET_DATA);

			for (AppDataCommand data : datas) {
				int minuteOrder = data.getMinuteOrder();
				long count = data.getAccessNumberSum();
				long sum = data.getResponseSumTimeSum();

				double avg = sum / count;
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public static class Statistics {
		private Date m_period;

		private long m_count;

		private double m_avg;

		public Date getPeriod() {
			return m_period;
		}

		public void setPeriod(Date period) {
			m_period = period;
		}

		public long getCount() {
			return m_count;
		}

		public void setCount(long count) {
			m_count = count;
		}

		public double getAvg() {
			return m_avg;
		}

		public void setAvg(double avg) {
			m_avg = avg;
		}
	}

}
