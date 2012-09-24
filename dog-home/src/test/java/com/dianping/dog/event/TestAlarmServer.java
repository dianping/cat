package com.dianping.dog.event;

import java.util.Date;

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

		RuleEntity ruleEntity1 = createRuleEntity("AutoauditMQ", 12, RuleType.Exception);

		RuleEntity ruleEntity2 = createRuleEntity("SocialRelationshipServer", 23423, RuleType.Exception);

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

	protected RuleEntity createRuleEntity(String domain, int ruleId, RuleType ruleType) {

		Duration duration1 = new Duration();
		duration1.setMin(Duration.INFINITESIMAL);
		duration1.setMax(2);
		duration1.setInterval(1000L * 60);
		duration1.addAlarmType(AlarmType.EMAIL);

		Duration duration2 = new Duration();
		duration2.setMin(2);
		duration2.setMax(Duration.INFINITY);
		duration2.setInterval(1000L * 60);
		duration1.addAlarmType(AlarmType.EMAIL);
		duration1.addAlarmType(AlarmType.SMS);

		RuleEntity ruleEntity = new RuleEntity();
		ruleEntity.setDomain(domain);
		ruleEntity.setGmtModified(new Date(System.currentTimeMillis()));
		ruleEntity.setId(ruleId);
		ruleEntity.setBaseUrl("http://cat.dianpingoa.com/cat/r/dashboard");
		ruleEntity.setConnectSource("Cat");
		ruleEntity.setReport("problem");
		ruleEntity.setType("heartbeat");
		ruleEntity.setRuleType(ruleType);

		ruleEntity.setPeriod(5);
		ruleEntity.addDuration(duration2);
		ruleEntity.addDuration(duration1);
		return ruleEntity;
	}
}
