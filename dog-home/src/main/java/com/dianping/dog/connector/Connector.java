package com.dianping.dog.connector;

import com.dianping.dog.event.EventDispatcher;



public interface Connector {
	
	ConnectorContext getConnectorContext();
	
	void init(ConnectorContext ctx);
	
	void setDispatcher(EventDispatcher dispatcher);
	
	long getConnectorId();
 
	boolean produceData();
	
}
