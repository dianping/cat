package com.dianping.cat.report.alert;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.report.task.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.task.alert.transaction.TransactionAlert;

public class AlertTest extends ComponentTestCase {

	@Before
	public void before() throws Exception{
		ServerConfigManager manager = lookup(ServerConfigManager.class);
		
		manager.initialize(new File("/data/appdatas/cat/server.xml"));
	}
	@Test
	public void testHeartbeat() {
		HeartbeatAlert alert = lookup(HeartbeatAlert.class);

		alert.run();
	}
	
	@Test
	public void testTransaction() {
		TransactionAlert alert = lookup(TransactionAlert.class);
		
		alert.run();
	}
	
}
