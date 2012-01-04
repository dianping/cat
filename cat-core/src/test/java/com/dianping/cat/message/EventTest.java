package com.dianping.cat.message;

import org.junit.Test;

import com.dianping.cat.Cat;

public class EventTest {
	public static MessageProducer CAT = Cat.getProducer();

	@Test
	public void testNormal() {
		Event event = CAT.newEvent("Review", "New");

		event.addData("id", 12345);
		event.addData("user", "john");
		event.setStatus(Message.SUCCESS);
		event.complete();
	}

	@Test
	public void testException() {
		CAT.logError(new RuntimeException());
	}

	@Test
	public void testInOneShot() {
		// Normal case
		CAT.logEvent("Review", "New", Message.SUCCESS, "id=12345&user=john");

		// Exception case
		CAT.logError(new RuntimeException());
	}
}
