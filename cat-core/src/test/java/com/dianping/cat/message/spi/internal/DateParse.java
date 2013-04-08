package com.dianping.cat.message.spi.internal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

public class DateParse {

	String formate = "yyyy-MM-dd HH:mm:ss.SSS";

	int s_second = 1000;

	int s_minute = 60 * s_second;

	int s_hour = 60 * s_minute;

	int s_day = 24 * s_hour;

	TimeTool unit;

	class TimeTool {
		private long m_time;

		private int m_year;

		private int m_month;

		private int m_day;

		private int m_hour;

		private int m_minute;

		private int m_second;

		private int m_milSecond;

		public TimeTool clone() {
			TimeTool tool = new TimeTool();
			tool.setTime(m_time);
			tool.setYear(m_year);
			tool.setMonth(m_month);
			tool.setDay(m_day);
			tool.setHour(m_hour);
			tool.setMinute(m_minute);
			tool.setSecond(m_second);
			tool.setMilSecond(m_milSecond);
			return tool;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder(20);
			sb.append(m_year).append('-');
			sb.append(m_month + 1 < 10 ? "0" + (1 + m_month) : m_month).append('-');
			sb.append(m_day < 10 ? "0" + (m_day) : m_day).append(' ');
			sb.append(m_hour < 10 ? "0" + m_hour : m_hour).append(':');
			sb.append(m_minute < 10 ? "0" + m_minute : m_minute).append(':');
			sb.append(m_second < 10 ? "0" + m_second : m_second).append('.');
			if (m_milSecond < 10) {
				sb.append("00" + m_milSecond);
			} else if (m_milSecond < 100) {
				sb.append("0" + m_milSecond);
			} else {
				sb.append(m_milSecond);
			}
			return sb.toString();
		}

		private TimeTool addMilSecond(long util) {
			int milsecond = (int) (util % s_second);
			int second = (int) (util / s_second);
			m_milSecond = m_milSecond + milsecond;
			if (m_milSecond >= 1000) {
				m_milSecond = m_milSecond - 1000;
			}

			m_second = m_second + second;
			if (m_second >= 60) {
				m_second = m_second % 60;
				int duration = m_second / 60;

				m_minute += duration;
				if (m_minute >= 60) {
					m_minute = m_minute % 60;
					int hourDuration = m_minute / 60;

					m_hour = m_hour + hourDuration;
				}
			}
			return this;
		}

		public long getTime() {
			return m_time;
		}

		public void setTime(long time) {
			m_time = time;
		}

		public int getYear() {
			return m_year;
		}

		public void setYear(int year) {
			m_year = year;
		}

		public int getMonth() {
			return m_month;
		}

		public void setMonth(int month) {
			m_month = month;
		}

		public int getDay() {
			return m_day;
		}

		public void setDay(int day) {
			m_day = day;
		}

		public int getHour() {
			return m_hour;
		}

		public void setHour(int hour) {
			m_hour = hour;
		}

		public int getMinute() {
			return m_minute;
		}

		public void setMinute(int minute) {
			m_minute = minute;
		}

		public int getSecond() {
			return m_second;
		}

		public void setSecond(int second) {
			m_second = second;
		}

		public int getMilSecond() {
			return m_milSecond;
		}

		public void setMilSecond(int milSecond) {
			m_milSecond = milSecond;
		}
	}

	@Test
	public void test() {
		long time = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);

		unit = new TimeTool();
		unit.setTime(time);
		unit.setYear(cal.get(Calendar.YEAR));
		unit.setMonth(cal.get(Calendar.MONTH));
		unit.setDay(cal.get(Calendar.DAY_OF_MONTH));
		unit.setHour(cal.get(Calendar.HOUR_OF_DAY));
		unit.setMinute(cal.get(Calendar.MINUTE));
		unit.setSecond(cal.get(Calendar.SECOND));
		unit.setMilSecond(cal.get(Calendar.MILLISECOND));

		System.out.println(formatByDefault(time));
		System.out.println(format(time));

		int size = 0;
		while (size < 10000000) {
			Assert.assertEquals(formatByDefault(time), format(time));
			size++;
		}
	}

	private String format(long time) {
		return unit.clone().addMilSecond(time - unit.getTime()).toString();
	}

	private String formatByDefault(long time) {
		return new SimpleDateFormat(formate).format(new Date(time));
	}
}
