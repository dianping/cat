package com.dianping.dog.event;

import java.util.Calendar;

import org.junit.Test;

import com.dianping.dog.alarm.connector.ConnectorManager;
import com.dianping.dog.alarm.connector.DataService;
import com.dianping.dog.alarm.entity.ConnectEntity;
import com.dianping.dog.alarm.filter.ProblemFilterListener;
import com.dianping.dog.alarm.merge.ProblemMergeListener;
import com.site.lookup.ComponentTestCase;

public class TestDataParser extends ComponentTestCase{
	
	@Test
	public void testDataParser() throws Exception{
	   ConnectEntity entity = new ConnectEntity();
	   entity.setConId(1);
	   entity.setBaseUrl("http://cat.dianpingoa.com/cat/r/dashboard");
	   entity.setConnectSource("Cat");
	   entity.setDomain("AutoauditMQ");
	   entity.setReport("problem");
		entity.setGmtModified(Calendar.getInstance().getTime());
		entity.setName("ProblemConnectorComponent");
		entity.setType("heartbeat");
		
		ConnectorManager dataMananger = lookup(ConnectorManager.class);
		dataMananger.registerConnector(entity);
		
		EventListenerRegistry listenerRegistry = lookup(EventListenerRegistry.class);
		
		ProblemFilterListener problemFilterListener = lookup(ProblemFilterListener.class);
		listenerRegistry.register(problemFilterListener);
		
		ProblemMergeListener problemMergeListener = lookup(ProblemMergeListener.class);
		listenerRegistry.register(problemMergeListener);
		
		DataService dataService = lookup(DataService.class);
		dataService.init();
		dataService.start();
		System.in.read();
		
	}
}
