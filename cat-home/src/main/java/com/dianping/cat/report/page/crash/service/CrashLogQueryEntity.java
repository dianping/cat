package com.dianping.cat.report.page.crash.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;
import com.dianping.cat.config.Level;
import com.dianping.cat.helper.TimeHelper;

public class CrashLogQueryEntity {

	private String m_day;

	private String m_startTime;

	private String m_endTime;

	private String m_appName = "1";

	private String m_module;

	private int m_platform = 1;

	private String m_dpid = null;

	private String m_msg = null;

	private String m_appVersion;

	private String m_platformVersion;

	private String m_level = null;

	private String m_device = null;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private SimpleDateFormat m_day_format = new SimpleDateFormat("yyyy-MM-dd");

	private String m_query;

	private int m_step;

	public CrashLogQueryEntity() {
		super();
	}

	public CrashLogQueryEntity(String origin) {
		List<String> strs = Splitters.by(";").split(origin);

		try {
			m_day = strs.get(0);
			m_startTime = strs.get(1);
			m_endTime = strs.get(2);
			m_appName = strs.get(3);
			m_appVersion = strs.get(4);
			m_platformVersion = strs.get(5);
			m_module = strs.get(6);
			m_platform = Integer.valueOf(strs.get(7));
		} catch (Exception e) {
			Cat.logError(e);
		}
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

	public Date buildTrendStartTime() {
		if (StringUtils.isNotBlank(m_startTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_startTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentDay();
	}

	public String getQuery() {
		return m_query;
	}

	public void setQuery(String query) {
		m_query = query;
	}

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

	public String getDay() {
		return m_day;
	}

	public void setDay(String day) {
		m_day = day;
	}

	public String getDpid() {
		if (StringUtils.isEmpty(m_dpid)) {
			return null;
		}

		return m_dpid;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
	}

	public String getAppName() {
		return m_appName;
	}

	public void setAppName(String appName) {
		m_appName = appName;
	}

	public String getStartTime() {
		return m_startTime;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public String getEndTime() {
		return m_endTime;
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public String getModule() {
		return m_module;
	}

	public void setModule(String module) {
		m_module = module;
	}

	public int getPlatform() {
		return m_platform;
	}

	public void setPlatform(int platform) {
		m_platform = platform;
	}

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	public String getAppVersion() {
		return m_appVersion;
	}

	public void setAppVersion(String appVersion) {
		m_appVersion = appVersion;
	}

	public String getPlatformVersion() {
		return m_platformVersion;
	}

	public void setPlatformVersion(String platformVersion) {
		m_platformVersion = platformVersion;
	}

	public int getLevel() {
		if (StringUtils.isNotBlank(m_level)) {
			return Level.getCodeByName(m_level);
		} else {
			return -1;
		}
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public String getDevice() {
		return m_device;
	}

	public void setDevice(String device) {
		m_device = device;
	}

	public SimpleDateFormat getFormat() {
		return m_format;
	}

	public void setFormat(SimpleDateFormat format) {
		m_format = format;
	}

	public int getStep() {
		return m_step;
	}

	public void setStep(int step) {
		m_step = step;
	}

}
