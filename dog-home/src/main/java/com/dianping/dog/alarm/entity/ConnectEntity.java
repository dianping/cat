package com.dianping.dog.alarm.entity;

import java.util.Date;

import com.dianping.dog.alarm.connector.ConnectorType;

public class ConnectEntity implements Comparable<ConnectEntity>{
	
	private long conId;
	
	private String connectSource;// cat

	private String domain;

	private String reportType;

	private String type;

	private String name;
	
	private String baseUrl;
	
	private Date gmtModified;
	
	public ConnectorType getConType(){
		if(baseUrl.startsWith("http://")){
			return ConnectorType.HTTP;
		}else{
			return ConnectorType.UNSUPPORT;
		}
	}
	
	public String getUrl() {
	   StringBuilder sb = new StringBuilder();
	   sb.append(this.baseUrl);
	   sb.append("?");
	   sb.append(String.format("%s=%s", "report",this.reportType));
	   sb.append(String.format("&%s=%s", "domain",this.domain));
	   sb.append(String.format("&%s=%s", "type",this.type));
   	return sb.toString();
   }

	public void setBaseUrl(String url) {
   	this.baseUrl = url;
   }

	public long getConId() {
   	return conId;
   }

	public void setConId(long conId) {
   	this.conId = conId;
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

	
	public String getReportType() {
   	return reportType;
   }

	public void setReportType(String reportType) {
   	this.reportType = reportType;
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
