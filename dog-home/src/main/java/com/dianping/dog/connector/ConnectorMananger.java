package com.dianping.dog.connector;

import com.dianping.dog.alarm.rule.ConnectEntity;
import com.dianping.dog.alarm.rule.RuleEntity;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.RuleEvent;
import com.site.lookup.annotation.Inject;

public class ConnectorMananger implements EventListener<RuleEvent> {
	
	@Inject
	private ConnectorRegistry m_conRegistry;

	@Override
   public boolean isEligible(RuleEvent event) {
	   return true;
   }

	@Override
   public void onEvent(RuleEvent event) {
		RuleEntity entity = event.getRuleEntity();
		ConnectEntity con = entity.getConnect();
		ConnectorContext ctx = new ConnectorContext(con);
	   m_conRegistry.registerConnector(ctx);
   }
	
	public void setConRegistry(ConnectorRegistry conRegistry){
		m_conRegistry = conRegistry;
	}

}
