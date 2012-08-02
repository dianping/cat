package com.dianping.cat.notify.model;

import java.util.Date;

public class DailyReport {

	private int id;

	private int type;

	private String name;

	private String ip;

	private String domain;

	private Date period;

	private String content;

	private Date creation_date;

	public static String PROBLEM_REPORT = "problem";

	public static String EVENT_REPORT = "event";

	public static String TRANSACGION_REPORT = "transaction";

	public static int JSON_TYPE = 2;

	public static int XML_TYPE = 1;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Date getPeriod() {
		return period;
	}

	public void setPeriod(Date period) {
		this.period = period;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreation_date() {
		return creation_date;
	}

	public void setCreation_date(Date creation_date) {
		this.creation_date = creation_date;
	}

}
