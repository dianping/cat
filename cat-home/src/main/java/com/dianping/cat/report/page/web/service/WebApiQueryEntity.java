package com.dianping.cat.report.page.web.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class WebApiQueryEntity {

	public static final int DEFAULT_COMMAND = 1;

	protected Date m_date;

	public static final int DEFAULT_VALUE = -1;

	protected int m_id;

	protected int m_city = DEFAULT_VALUE;

	protected int m_operator = DEFAULT_VALUE;

	private int m_code = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public WebApiQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
		m_id = DEFAULT_COMMAND;
	}

	public WebApiQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(1));
			m_code = parseValue(strs.get(2));
			m_city = parseValue(strs.get(3));
			m_operator = parseValue(strs.get(4));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(5));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(6));
		} catch (Exception e) {
			Cat.logError(e);
		}
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

	public int getCode() {
		return m_code;
	}

	public Date getDate() {
		return m_date;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}

	public int getId() {
		return m_id;
	}

	public int getOperator() {
		return m_operator;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
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

	public void setCity(int city) {
		m_city = city;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	public void setEndMinuteOrder(int endMinuteOrder) {
		m_endMinuteOrder = endMinuteOrder;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setOperator(int operator) {
		m_operator = operator;
	}

	public void setStartMinuteOrder(int startMinuteOrder) {
		m_startMinuteOrder = startMinuteOrder;
	}
}