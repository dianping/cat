package com.dianping.dog.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dog.entity.ConnectEntity;
import com.dianping.dog.entity.RuleEntity;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.RuleEvent;

public class ConnectorMananger implements ConnectorRegistry, EventListener<RuleEvent> {
	
	private EventDispatcher m_dispatcher;

	Map<Long, Connector> m_connectors = new HashMap<Long, Connector>();

	@Override
	public List<Connector> getConnectors() {
		List<Connector> connectorList = null;
		synchronized (m_connectors) {
			connectorList = new ArrayList<Connector>(m_connectors.values());
		}
		return connectorList;
	}

	@Override
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
				con.setDispatcher(m_dispatcher);
			}
			m_connectors.put(ctx.getRuleId(), con);
		}
	}

	@Override
	public void removeConnector(long connectorId) {
		synchronized (m_connectors) {
			m_connectors.remove(connectorId);
		}
	}
	
   public void setDispatcher(EventDispatcher dispatcher) {
   	m_dispatcher = dispatcher;
   }
	
	@Override
   public void onEvent(RuleEvent event) {
		RuleEntity entity = event.getRuleEntity();
		ConnectEntity con = entity.getConnect();
		ConnectorContext ctx = new ConnectorContext(con);
		registerConnector(ctx);
   }

}
