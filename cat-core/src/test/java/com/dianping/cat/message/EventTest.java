package com.dianping.cat.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class EventTest {
	@Test
	public void testNormal() {
		Event event = Cat.getProducer().newEvent("Review", "New");

		event.addData("id", 12345);
		event.addData("user", "john");
		event.setStatus(Message.SUCCESS);
		event.complete();
	}

	@Test
	public void testException() {
		Cat.getProducer().logError(new RuntimeException());
	}

	@Test
	public void testInOneShot() {
		// Normal case
		Cat.getProducer().logEvent("Review", "New", Message.SUCCESS, "id=12345&user=john");

		// Exception case
		Cat.getProducer().logError(new RuntimeException());
	}
}
