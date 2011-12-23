package com.dianping.cat.message;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

import com.dianping.cat.Cat;

public class EventTest {
	public static MessageFactory CAT = Cat.getFactory();

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
		Exception e = new RuntimeException();
		Event event = CAT.newEvent("ERROR", e.getClass().getName());

		event.addData(toString(e));
		event.setStatus("-1");
		event.complete();
	}

	@Test
	public void testInOneShot() {
		// Normal case
		CAT.logEvent("Review", "New", Message.SUCCESS, "id=12345&user=john");

		// Exception case
		Exception e = new RuntimeException();

		CAT.logEvent("Exception", e.getClass().getName(), Message.SUCCESS, toString(e));
	}

	private String toString(Exception e) {
		StringWriter writer = new StringWriter(2048);

		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
