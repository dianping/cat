package com.dianping.dog.connector;

import com.dianping.dog.alarm.rule.ConnectEntity;

public class ConnectorContext implements Comparable<ConnectorContext> {
	
	private ConnectorType type;
	
	private ConnectEntity conEntity;
	
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

	@Override
	public int compareTo(ConnectorContext o) {
		return 0;
	}

}
