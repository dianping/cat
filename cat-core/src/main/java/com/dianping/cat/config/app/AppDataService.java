package com.dianping.cat.config.app;

import java.util.Date;

public class AppDataService {
	
	public void insert(){
		
	}

	public void queryAvg(QueryEntity entity) {

	}

	public void queryCount(QueryEntity entity) {

	}

	public void querySuccessRate(QueryEntity entity) {
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
