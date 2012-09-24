package com.dianping.dog.alarm.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.parser.DataParserFactory;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.site.lookup.annotation.Inject;

public class ConnectorManager implements LogEnabled{
	
	@Inject
	private DataParserFactory m_dataParserFactory;
	
	@Inject
	private RuleManager m_ruleMananger;
	
	private Logger m_logger;
	
	Map<Integer, Connector> m_connectors = new HashMap<Integer, Connector>();
	
	public List<Connector> getConnectors() {
		List<Rule> rules =  m_ruleMananger.getRules();
		Set<Integer> updatedIds = new HashSet<Integer>();
		for(Rule rule: rules){
			registerConnector(rule.getRuleEntity().getConnect());
			updatedIds.add(rule.getRuleId());
		}
		List<Connector> connectorList = null;
		synchronized (m_connectors) {
			connectorList = new ArrayList<Connector>();
			Iterator<Connector> cons =  m_connectors.values().iterator();
			while(cons.hasNext()){
				Connector current = cons.next();
				if(!updatedIds.contains(current.getConnectorId())){
					cons.remove();
					m_logger.info(String.format("remove connector id:[%s]",current.getConnectorId()));
				}else{
					connectorList.add(current);
				}
			}
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
			m_logger.info(String.format("add connector id:[%s]",entity.getConId()));
			m_connectors.put(entity.getConId(), con);
		}
	}

	public void removeConnector(long connectorId) {
		synchronized (m_connectors) {
			m_connectors.remove(connectorId);
		}
	}

	@Override
   public void enableLogging(Logger logger) {
		this.m_logger = logger;	   
   }

}
