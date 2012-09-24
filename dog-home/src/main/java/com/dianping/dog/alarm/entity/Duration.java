package com.dianping.dog.alarm.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.dog.alarm.rule.AlarmType;

public class Duration {

	public static int INFINITY = -1;

	public static int INFINITESIMAL = -2;

	private int min;

	private int max;
	
	private String id;

	private long interval;

	private Date gmtModified;

	private List<AlarmType> alarmType;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		if (min == INFINITESIMAL || min >= 0) {
			this.min = min;
		} else {
			throw new NullPointerException("min must >=0 or -2");
		}
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		if (max == INFINITY || max >= 0) {
			this.max = max;
		} else {
			throw new NullPointerException("max must >=0 or -1");
		}
	}

	/**
	 * (min< count <= max) return true
	 * */
	public boolean isIn(int count) {
		if (min == INFINITESIMAL && max == INFINITY) {
			return true;
		}
		if (min == INFINITESIMAL) {
			return count <= max ? true : false;
		}
		if (max == INFINITY) {
			return count > min ? true : false;
		}
		if (min < count && count <= max) {
			return true;
		}
		return false;
	}

	public List<AlarmType> getAlarmType() {
		return alarmType;
	}

	public void addAlarmType(AlarmType type) {
		if (alarmType == null) {
			alarmType = new ArrayList<AlarmType>();
		}
		alarmType.add(type);
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public String getId() {
   	return id;
   }

	public void setId(String id) {
   	this.id = id;
   }

	public int hashCode() {
		StringBuffer sb = new StringBuffer();
		sb.append(min + "@");
		sb.append(max + "@");
		return sb.toString().hashCode();
	}

}
