package com.dianping.dog.connector;

import java.util.List;

public interface ConnectorRegistry {

	public List<Connector> getConnectors() ;

	public void registerConnector(ConnectorContext ctx);

	public void removeConnector(long connectorId);
	
}
