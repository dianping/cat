package com.dianping.dog.alarm.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectorManager {
	

	Map<Long, Connector> m_connectors = new HashMap<Long, Connector>();

	public List<Connector> getConnectors() {
		List<Connector> connectorList = null;
		synchronized (m_connectors) {
			connectorList = new ArrayList<Connector>(m_connectors.values());
		}
		return connectorList;
	}

	public void registerConnector(ConnectorContext ctx) {
		synchronized (m_connectors) {
			Connector con = m_connectors.get(ctx.getRuleId());
			if (con != null) {
				if (con.getConnectorContext().compareTo(ctx) == 0) {
					return;
				}
				m_connectors.remove(ctx.getRuleId());
			}
			if (ctx.getType() == ConnectorType.HTTP) {
				con = new HttpConnector();
				con.init(ctx);
			}
			m_connectors.put(ctx.getRuleId(), con);
		}
	}

	public void removeConnector(long connectorId) {
		synchronized (m_connectors) {
			m_connectors.remove(connectorId);
		}
	}

}
