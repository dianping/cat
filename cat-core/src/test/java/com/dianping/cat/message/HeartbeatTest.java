package com.dianping.cat.message;

import org.junit.Test;

import com.dianping.cat.Cat;

public class HeartbeatTest {
	public static MessageFactory CAT = Cat.getFactory();

	@Test
	public void testStatus() {
		Heartbeat heartbeat = CAT.newHeartbeat("System", "Status");

		heartbeat.addData("ip", "192.168.10.111");
		heartbeat.addData("host", "host-1");
		heartbeat.addData("load", "2.1");
		heartbeat.addData("cpu", "0.12,0.10");
		heartbeat.addData("memory.total", "2G");
		heartbeat.addData("memory.free", "456M");
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();
	}

	@Test
	public void testService() {
		Heartbeat heartbeat = CAT.newHeartbeat("Service", "ReviewService");

		heartbeat.addData("host", "192.168.10.112:1234");
		heartbeat.addData("weight", "20");
		heartbeat.addData("visits", "12345");
		heartbeat.addData("manifest", "addReview,getReview,getShopReviews");
		heartbeat.addData("more", "...");
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();
	}

	@Test
	public void testInOneShot() {
		CAT.logHeartbeat("System", "Status", "0",
		      "ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M");
	}
}
