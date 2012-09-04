package com.dianping.dog.connector;

import java.util.List;


public interface ConnectorRegistry {
	
	List<Connector> getConnectors();
	
	void removeConnector(long connectorId);
	
	void registerConnector(ConnectorContext ruleEntity);
	
}
