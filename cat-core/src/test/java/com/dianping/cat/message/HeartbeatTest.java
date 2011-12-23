package com.dianping.cat.message;

import org.junit.Test;

import com.dianping.cat.Cat;

public class HeartbeatTest {
	public static MessageFactory CAT = Cat.getFactory();

	@Test
	public void testStatus() {
		Heartbeat event = CAT.newHeartbeat("System", "Status");

		event.addData("ip", "192.168.10.111");
		event.addData("host", "host-1");
		event.addData("load", "2.1");
		event.addData("cpu", "0.12,0.10");
		event.addData("memory.total", "2G");
		event.addData("memory.free", "456M");
		event.setStatus("0");
		event.complete();
	}

	@Test
	public void testService() {
		Heartbeat event = CAT.newHeartbeat("Service", "ReviewService");

		event.addData("host", "192.168.10.112:1234");
		event.addData("weight", "20");
		event.addData("visits", "12345");
		event.addData("manifest", "addReview,getReview,getShopReviews");
		event.addData("more", "...");
		event.setStatus("0");
		event.complete();
	}

	@Test
	public void testInOneShot() {
		CAT.logHeartbeat("System", "Status", "0",
		      "ip=192.168.10.111&host=host-1&load=2.1&cpu=0.12,0.10&memory.total=2G&memory.free=456M");
	}
}
