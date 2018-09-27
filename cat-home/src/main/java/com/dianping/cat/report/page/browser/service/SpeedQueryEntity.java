package com.dianping.cat.report.page.browser.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class SpeedQueryEntity {

	public static final int DEFAULT_VALUE = -1;

	private Date m_date;

	private int m_network = DEFAULT_VALUE;

	private int m_platform = DEFAULT_VALUE;

	private int m_city = DEFAULT_VALUE;

	private int m_operator = DEFAULT_VALUE;

	private String m_pageId;

	private int m_stepId = DEFAULT_VALUE;

	private int m_source = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public SpeedQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
	}

	public SpeedQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			String[] pageIdPair = strs.get(1).split("\\|");
			m_pageId = pageIdPair[1];
			m_stepId = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_platform = parseValue(strs.get(4));
			m_city = parseValue(strs.get(5));
			m_operator = parseValue(strs.get(6));
			m_source = parseValue(strs.get(7));

			if (strs.size() > 8) {
				m_startMinuteOrder = convert2MinuteOrder(strs.get(8));
				m_endMinuteOrder = convert2MinuteOrder(strs.get(9));
			}
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

	public Date getDate() {
		return m_date;
	}

	public int getNetwork() {
		return m_network;
	}

	public int getOperator() {
		return m_operator;
	}

	public int getSource() {
		return m_source;
	}

	public int getPlatform() {
		return m_platform;
	}

	public String getPageId() {
		return m_pageId;
	}

	public int getStepId() {
		return m_stepId;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}

	private Date parseDate(String dateStr) throws Exception {
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

	private int parseValue(String str) {
		if (StringUtils.isEmpty(str)) {
			return DEFAULT_VALUE;
		} else {
			return Integer.parseInt(str);
		}
	}

	public void setPageId(String pageId) {
		m_pageId = pageId;
	}

	public void setStepId(int stepId) {
		m_stepId = stepId;
	}

}