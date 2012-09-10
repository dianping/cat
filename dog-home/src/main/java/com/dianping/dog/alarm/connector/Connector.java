package com.dianping.dog.alarm.connector;

import java.util.Date;



public interface Connector {
	
	ConnectorContext getConnectorContext();
	
	void init(ConnectorContext ctx);
	
	long getConnectorId();
 
	RowData produceData(Date currentTime);
	
}
