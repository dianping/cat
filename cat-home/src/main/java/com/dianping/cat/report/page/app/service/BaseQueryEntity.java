package com.dianping.cat.report.page.app.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.util.StringUtils;

public class BaseQueryEntity {

	protected Date m_date;

	public static final int DEFAULT_VALUE = -1;

	protected int m_id;

	protected int m_network = DEFAULT_VALUE;

	protected int m_version = DEFAULT_VALUE;

	protected int m_platfrom = DEFAULT_VALUE;

	protected int m_city = DEFAULT_VALUE;

	protected int m_operator = DEFAULT_VALUE;

	public BaseQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
	}

	protected int convert2MinuteOrder(String time) {
		int current = DEFAULT_VALUE;

		if (StringUtils.isNotEmpty(time)) {
			try {
				current = Integer.parseInt(time);
			} catch (NumberFormatException e) {
				String[] pair = time.split(":");
				int hour = Integer.parseInt(pair[0]);
				int minute = Integer.parseInt(pair[1]);
				current = hour * 60 + minute;
				current = current - current % 5;
			}
		}
		return current;
	}

	public int getCity() {
		return m_city;
	}

	public Date getDate() {
		return m_date;
	}

	public int getId() {
		return m_id;
	}

	public int getNetwork() {
		return m_network;
	}

	public int getOperator() {
		return m_operator;
	}

	public int getPlatfrom() {
		return m_platfrom;
	}

	public int getVersion() {
		return m_version;
	}

	protected Date parseDate(String dateStr) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if (StringUtils.isNotEmpty(dateStr)) {
			return sdf.parse(dateStr);
		} else {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}
	}

	protected int parseValue(String str) {
		if (StringUtils.isEmpty(str)) {
			return DEFAULT_VALUE;
		} else {
			return Integer.parseInt(str);
		}
	}

	public void setId(int id) {
		m_id = id;
	}
}
