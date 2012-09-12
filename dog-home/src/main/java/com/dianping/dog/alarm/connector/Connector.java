package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;



public interface Connector {
	
	void init(ConnectEntity entity);
	
	ConnectEntity getConnectorEntity();
	
	long getConnectorId();
 
	RowData produceData(Date currentTime);
	
}
