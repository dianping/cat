package com.dianping.dog.entity;

public class ConnectEntity {
	
	private String connectType;// http

	private String connectSource;// cat

	private String domain;

	private String report;

	private String type;

	private String name;

	public String getConnectType() {
		return connectType;
	}

	public void setConnectType(String connectType) {
		this.connectType = connectType;
	}

	public String getConnectSource() {
		return connectSource;
	}

	public void setConnectSource(String connectSource) {
		this.connectSource = connectSource;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
