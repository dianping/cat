package com.dianping.cat.config.app;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.app.AppDataCommandEntity;

public class AppDataService {

	@Inject
	private AppDataCommandDao m_dao;

	public void insert() {
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

			}
		} catch (DalException e) {
			Cat.logError(e);
		}
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
