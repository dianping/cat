package com.dianping.cat.report.page.app.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;

public class DailyCommandQueryEntity extends CommandQueryEntity {

	private Date m_endDate;

	public DailyCommandQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_endDate = cal.getTime();

		cal.add(Calendar.DAY_OF_MONTH, -7);
		m_date = cal.getTime();

		m_id = DEFAULT_COMMAND;
	}

	public DailyCommandQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_endDate = parseDate(strs.get(1));
			m_id = parseValue(strs.get(2));
			m_code = parseValue(strs.get(3));
			m_network = parseValue(strs.get(4));
			m_version = parseValue(strs.get(5));
			m_connectType = parseValue(strs.get(6));
			m_platfrom = parseValue(strs.get(7));
			m_city = parseValue(strs.get(8));
			m_operator = parseValue(strs.get(9));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public Date getEndDate() {
		return m_endDate;
	}

	public void setEndDate(Date endDate) {
		m_endDate = endDate;
	}

}
