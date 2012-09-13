package com.dianping.dog.alarm.entity;

import java.util.Date;
import java.util.List;

import com.site.dal.jdbc.QueryDef;

/***
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 **/
public class RuleEntity {

	public static final QueryDef DELETE_BY_PK = null;

	private long id;

	private String domain;

	private String name;

	private String type;

	private String reportType;

	private List<Duration> durations;

	private ConnectEntity connect;

	private int period;

	private Date gmtModified;

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPeriod() {
   	return period;
   }

	public void setPeriod(int period) {
   	this.period = period;
   }

	public String getReportType() {
   	return reportType;
   }

	public List<Duration> getDurations() {
		return durations;
	}

	public void setDurations(List<Duration> durations) {
		this.durations = durations;
	}

	public ConnectEntity getConnect() {
		return connect;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

}
