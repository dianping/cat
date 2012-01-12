package com.dianping.cat.demo;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class Demo extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		File configFile = getResourceFile("client.xml");

		Cat.initialize(getContainer(), configFile);
		Cat.setup(null, null);
	}

	@After
	public void after() throws Exception {
		Cat.reset();
	}

	@Test
	public void demo() throws Exception {
		System.out.println(System.currentTimeMillis());
		System.out.println((long)(System.nanoTime() / 1e6));

		MessageProducer cat = lookup(MessageProducer.class);
		Transaction t = cat.newTransaction("URL", "FailureReportPage");

		cat.logEvent("Error", OutOfMemoryError.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", RuntimeException.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", NullPointerException.class.getName(), "ERROR", null);

		t.setStatus("0");
		t.complete();
	}
}
