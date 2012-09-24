package com.dianping.dog.alarm.connector;

import java.util.Date;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParserFactory;



public interface Connector {
	
	void init(ConnectEntity entity,DataParserFactory factory);
	
	ConnectEntity getConnectorEntity();
	
	int getConnectorId();
 
	RowData produceData(Date currentTime);
	
}
