package com.dianping.cat.report.page.browser.service;

import com.dianping.cat.Cat;
import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AjaxDataQueryEntity {

	public static final int DEFAULT_COMMAND = 1;

	protected Date m_date;

	public static final int DEFAULT_VALUE = -1;

	protected int m_id;

	protected int m_city = DEFAULT_VALUE;

	protected int m_operator = DEFAULT_VALUE;

	private int m_code = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	private int m_network = DEFAULT_VALUE;

	public AjaxDataQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
		m_id = DEFAULT_COMMAND;
	}

	public AjaxDataQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(1));
			m_code = parseValue(strs.get(2));
			m_city = parseValue(strs.get(3));
			m_operator = parseValue(strs.get(4));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(5));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(6));
			m_network = parseValue(strs.get(7));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public AjaxDataQueryEntity(Date date) {
		m_date = date;
		m_id = DEFAULT_COMMAND;
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

	public int getNetwork() {
		return m_network;
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

	public AjaxDataQueryEntity setCity(String city) {
		m_city = parseValue(city);
		return this;
	}

	public AjaxDataQueryEntity setCode(String code) {
		m_code = parseValue(code);
		return this;
	}

	public AjaxDataQueryEntity setDate(Date date) {
		m_date = date;
		return this;
	}

	public AjaxDataQueryEntity setEndMinuteOrder(int endMinuteOrder) {
		m_endMinuteOrder = endMinuteOrder;
		return this;
	}

	public AjaxDataQueryEntity setId(String id) {
		m_id = parseValue(id);
		return this;
	}

	public AjaxDataQueryEntity setNetwork(String network) {
		m_network = parseValue(network);
		return this;
	}

	public AjaxDataQueryEntity setOperator(String operator) {
		m_operator = parseValue(operator);
		return this;
	}

	public AjaxDataQueryEntity setStartMinuteOrder(int startMinuteOrder) {
		m_startMinuteOrder = startMinuteOrder;
		return this;
	}
}
