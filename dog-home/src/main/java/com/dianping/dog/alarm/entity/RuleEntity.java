package com.dianping.dog.alarm.entity;

import java.util.Date;
import java.util.List;

import com.dianping.dog.alarm.rule.RuleTemplateEntity;
import com.site.dal.jdbc.QueryDef;

/***
 * @author yanchun.yang 从数据库中读取的规则配置实体类
 **/
public class RuleEntity {

	public static final QueryDef DELETE_BY_PK = null;

	private static String CONNECT_SOURCE = "cat";

	private long id;

	private String domain;

	private String name;

	private String type;

	private String reportType;

	private List<Duration> durations;

	private ConnectEntity connect;

	private long interval;

	private RuleTemplateEntity ruleTemplateEntity;

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

	public long getInterval() {
		if (interval == 0) {
			interval = ruleTemplateEntity.getInterval();
		}
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}


	public static QueryDef getDeleteByPk() {
   	return DELETE_BY_PK;
   }

	public static String getCONNECT_SOURCE() {
   	return CONNECT_SOURCE;
   }

	public String getReportType() {
   	return reportType;
   }

	public List<Duration> getDurations() {
		if (durations == null || durations.size() == 0) {
			durations = ruleTemplateEntity.getDurations();
		}
		return durations;
	}

	public void setDurations(List<Duration> durations) {
		this.durations = durations;
	}

	public ConnectEntity getConnect() {
		connect = new ConnectEntity();
		connect.setDomain(domain);
		connect.setName(name);
		connect.setType(type);
		connect.setReportType(reportType);
		connect.setConnectSource(CONNECT_SOURCE);
		return connect;
	}

	public RuleTemplateEntity getRuleTemplateEntity() {
		return ruleTemplateEntity;
	}

	public void setRuleTemplateEntity(RuleTemplateEntity ruleTemplateEntity) {
		this.ruleTemplateEntity = ruleTemplateEntity;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

}
