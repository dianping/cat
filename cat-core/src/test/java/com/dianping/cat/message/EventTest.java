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
		event.setStatus("0");
		event.complete();
	}

	@Test
	public void testException() {
		Exception e = new RuntimeException();
		Event event = CAT.newEvent("Exception", e.getClass().getName());

		event.addData(toString(e));
		event.setStatus("0");
		event.complete();
	}

	@Test
	public void testInOneShot() {
		// Normal case
		CAT.logEvent("Review", "New", "0", "id=12345&user=john");

		// Exception case
		Exception e = new RuntimeException();

		CAT.logEvent("Exception", e.getClass().getName(), "0", toString(e));
	}

	private String toString(Exception e) {
		StringWriter writer = new StringWriter(2048);

		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
