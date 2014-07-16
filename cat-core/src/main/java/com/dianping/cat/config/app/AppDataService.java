package com.dianping.cat.config.app;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class AppDataService {

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

	private Map<String, double[]> queryDelayAvg(QueryEntity entity) {

		return makeMockValue(DELAY_AVG);
	}

	private Map<String, double[]> queryRequestCount(QueryEntity entity) {

		return makeMockValue(REQUEST_COUNT);
	}

	private Map<String, double[]> querySuccessRatio(QueryEntity entity) {

		return makeMockValue(SUCCESS_RATIO);
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
