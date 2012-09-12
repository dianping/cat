package com.dianping.dog.alarm.entity;

import java.util.Date;

import com.dianping.dog.alarm.connector.ConnectorType;

public class ConnectEntity implements Comparable<ConnectEntity>{
	
	private long conId;
	
	private String connectType;// http

	private String connectSource;// cat

	private String domain;

	private String report;

	private String type;

	private String name;
	
	private String url;
	
	private Date gmtModified;
	
	public ConnectorType getConType(){
		if(url.startsWith("http://")){
			return ConnectorType.HTTP;
		}else{
			return ConnectorType.UNSUPPORT;
		}
	}
	
	public String getUrl() {
   	return url;
   }

	public long getConId() {
   	return conId;
   }

	public void setConId(long conId) {
   	this.conId = conId;
   }

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

	public Date getGmtModified() {
   	return gmtModified;
   }

	public void setGmtModified(Date gmtModified) {
   	this.gmtModified = gmtModified;
   }

	@Override
   public int compareTo(ConnectEntity o) {
		return (int) (this.getGmtModified().getTime() - o.getGmtModified().getTime());
   }
	
}
