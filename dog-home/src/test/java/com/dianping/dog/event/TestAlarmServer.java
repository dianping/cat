package com.dianping.dog.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.dianping.dog.alarm.alert.AlertEventListener;
import com.dianping.dog.alarm.connector.ConnectorManager;
import com.dianping.dog.alarm.connector.DataService;
import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.alarm.filter.ProblemFilterListener;
import com.dianping.dog.alarm.merge.ProblemMergeListener;
import com.dianping.dog.alarm.rule.AlarmType;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.alarm.rule.RuleType;
import com.site.lookup.ComponentTestCase;

public class TestAlarmServer extends ComponentTestCase {

	@Test
	public void testAlarmServer() throws Exception {
				
		RuleEntity ruleEntity1 =  createRuleEntity("AutoauditMQ",System.currentTimeMillis() -1L,RuleType.Exception);
		
		RuleEntity ruleEntity2 =  createRuleEntity("SocialRelationshipServer",System.currentTimeMillis() -999999L,RuleType.Exception);
		

		ConnectorManager dataMananger = lookup(ConnectorManager.class);
		dataMananger.registerConnector(ruleEntity1.getConnect());
		dataMananger.registerConnector(ruleEntity2.getConnect());
		
		RuleManager ruleMananger = lookup(RuleManager.class);
		ruleMananger.addRule(ruleEntity1);
		ruleMananger.addRule(ruleEntity2);

		EventListenerRegistry listenerRegistry = lookup(EventListenerRegistry.class);

		ProblemFilterListener problemFilterListener = lookup(ProblemFilterListener.class);
		listenerRegistry.register(problemFilterListener);

		ProblemMergeListener problemMergeListener = lookup(ProblemMergeListener.class);
		listenerRegistry.register(problemMergeListener);
		
		AlertEventListener alertEventListener = lookup(AlertEventListener.class);
		listenerRegistry.register(alertEventListener);

		DataService dataService = lookup(DataService.class);
		dataService.init();
		dataService.start();
		System.in.read();

	}

	protected RuleEntity createRuleEntity(String domain,long ruleId,RuleType ruleType) {
	
		Duration duration1 = new Duration();
		List<AlarmType> alarmTypeList = new ArrayList<AlarmType>();
		alarmTypeList.add(AlarmType.EMAIL);
		duration1.setMin(Duration.INFINITESIMAL);
		duration1.setMax(2);
		duration1.setInterval(1000L*60);
		duration1.setAlarmType(alarmTypeList);

		Duration duration2 = new Duration();
		List<AlarmType> alarmTypeList2 = new ArrayList<AlarmType>();
		alarmTypeList2.add(AlarmType.EMAIL);
		alarmTypeList2.add(AlarmType.SMS);
		duration2.setMin(2);
		duration2.setMax(Duration.INFINITY);
		duration2.setInterval(1000L*60);
		duration2.setAlarmType(alarmTypeList2);

		RuleEntity ruleEntity = new RuleEntity();
		ruleEntity.setDomain(domain);
		ruleEntity.setGmtModified(new Date(System.currentTimeMillis()));
		ruleEntity.setId(ruleId);
		ruleEntity.setBaseUrl("http://cat.dianpingoa.com/cat/r/dashboard");
		ruleEntity.setConnectSource("Cat");
		ruleEntity.setReport("problem");
		ruleEntity.setType("heartbeat");ruleEntity.setRuleType(ruleType);
		
		ruleEntity.setPeriod(5);
		ruleEntity.addDuration(duration2);
		ruleEntity.addDuration(duration1);
	   return ruleEntity;
   }
}
