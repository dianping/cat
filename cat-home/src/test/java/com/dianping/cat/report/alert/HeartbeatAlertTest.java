package com.dianping.cat.report.alert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.alert.heartbeat.HeartbeatAlert;

public class HeartbeatAlertTest extends ComponentTestCase {

	@Test
	public void test() {
		HeartbeatAlert alert = lookup(HeartbeatAlert.class);

		alert.run();
	}

}
