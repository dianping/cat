package com.dianping.dog.alarm.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParserFactory;
import com.site.lookup.annotation.Inject;

public class ConnectorManager {
	
	@Inject
	private DataParserFactory m_dataParserFactory;

	Map<Long, Connector> m_connectors = new HashMap<Long, Connector>();

	public List<Connector> getConnectors() {
		List<Connector> connectorList = null;
		synchronized (m_connectors) {
			connectorList = new ArrayList<Connector>(m_connectors.values());
		}
		return connectorList;
	}

	public void registerConnector(ConnectEntity entity) {
		synchronized (m_connectors) {
			Connector con = m_connectors.get(entity.getConId());
			if (con != null) {
				if (con.getConnectorEntity().compareTo(entity) == 0) {
					return;
				}
				m_connectors.remove(entity.getConId());
			}
			if (entity.getConType() == ConnectorType.HTTP) {
				con = new HttpConnector();
				con.init(entity,m_dataParserFactory);
			}
			m_connectors.put(entity.getConId(), con);
		}
	}

	public void removeConnector(long connectorId) {
		synchronized (m_connectors) {
			m_connectors.remove(connectorId);
		}
	}

}
