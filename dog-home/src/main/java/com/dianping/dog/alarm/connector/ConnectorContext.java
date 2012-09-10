package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;

public class ConnectorContext implements Comparable<ConnectorContext> {
	
	private ConnectorType type;
	
	private ConnectEntity conEntity;
	
	private Date time;
	
	private String url;
	
	@SuppressWarnings("unused")
   private ConnectorContext() {
   }

	public ConnectorContext(ConnectEntity con) {
		conEntity = con;
   }
	
	public long getRuleId(){
		return 0;
	}
	
	public ConnectorType getType(){
		return type;
	}
	
	public String getUrl(){
		return url;
	}

	public ConnectEntity getConEntity() {
   	return conEntity;
   }

	public void setConEntity(ConnectEntity conEntity) {
   	this.conEntity = conEntity;
   }

	public Date getTime() {
   	return time;
   }

	public Date getModifiedTime(){
		return this.conEntity.getGmtModified();
	}
	
	public void touch(Date time) {
   	this.time = time;
   }

	@Override
	public int compareTo(ConnectorContext o) {
		long t1 = this.getModifiedTime().getTime();
		long t2 = o.getModifiedTime().getTime();
		return (int)(t1 - t2);
	}

}
