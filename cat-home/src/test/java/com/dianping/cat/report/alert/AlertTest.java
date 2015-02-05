package com.dianping.cat.report.alert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.task.alert.transaction.TransactionAlert;

public class AlertTest extends ComponentTestCase {

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
