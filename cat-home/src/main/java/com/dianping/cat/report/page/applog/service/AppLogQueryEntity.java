package com.dianping.cat.report.page.applog.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.config.LogLevel;
import com.dianping.cat.helper.TimeHelper;

public class AppLogQueryEntity {

	private String m_day;

	private String m_startTime;

	private String m_endTime;

	private int m_appName = 1;

	private int m_platform = 1;

	private String m_unionId = null;

	private String m_category = null;

	private String m_appVersion;

	private String m_platformVersion;

	private String m_level;

	private String m_device = null;

	private String m_query;

	private int m_step;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private SimpleDateFormat m_day_format = new SimpleDateFormat("yyyy-MM-dd");

	public Date buildDay() {
		Date date = null;

		if (StringUtils.isNotBlank(m_day)) {
			try {
				date = m_day_format.parse(m_day);
			} catch (ParseException e) {
				date = TimeHelper.getCurrentDay();
			}
		} else {
			date = TimeHelper.getCurrentDay();
		}

		if (m_step != 0) {
			date = new Date(date.getTime() + m_step * TimeHelper.ONE_DAY);
		}
		return date;
	}

	public Date buildEndTime() {
		if (m_step != 0) {
			m_endTime = "23:59";
		}

		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_endTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_endTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentDay(1);
	}

	public Date buildStartTime() {
		if (m_step != 0) {
			m_day = m_day_format.format(buildDay());
			m_startTime = "00:00";
		}
		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_startTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_startTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentHour();
	}

	public int getAppName() {
		return m_appName;
	}

	public String getAppVersion() {
		return m_appVersion;
	}

	public String getCategory() {
		return m_category;
	}

	public String getDay() {
		return m_day;
	}

	public String getDevice() {
		return m_device;
	}

	public String getEndTime() {
		return m_endTime;
	}

	public int getLevel() {
		if (StringUtils.isNotBlank(m_level)) {
			return LogLevel.getId(m_level);
		} else {
			return -1;
		}
	}

	public int getPlatform() {
		return m_platform;
	}

	public String getPlatformVersion() {
		return m_platformVersion;
	}

	public String getQuery() {
		return m_query;
	}

	public String getStartTime() {
		return m_startTime;
	}

	public int getStep() {
		return m_step;
	}

	public String getUnionId() {
		if (StringUtils.isEmpty(m_unionId)) {
			return null;
		}
		return m_unionId;
	}

	public void setAppName(int appName) {
		m_appName = appName;
	}

	public void setAppVersion(String appVersion) {
		m_appVersion = appVersion;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setDay(String day) {
		m_day = day;
	}

	public void setDevice(String device) {
		m_device = device;
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public void setPlatform(int platform) {
		m_platform = platform;
	}

	public void setPlatformVersion(String platformVersion) {
		m_platformVersion = platformVersion;
	}

	public void setQuery(String query) {
		m_query = query;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public void setStep(int step) {
		m_step = step;
	}

	public void setUnionId(String unionId) {
		m_unionId = unionId;
	}

}
