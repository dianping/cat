package com.dianping.cat.demo;

import java.io.File;
import java.util.Random;

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
	private static boolean s_initialized;

	@Before
	public void before() throws Exception {
		if (!s_initialized) {
			File configFile = getResourceFile("client.xml");

			s_initialized = true;
			Cat.initialize(getContainer(), configFile);
		}

		Cat.setup(null);
	}

	@After
	public void after() throws Exception {
		Cat.reset();
	}

	@Test
	public void demo() throws Exception {
		MessageProducer cat = lookup(MessageProducer.class);
		Transaction t = cat.newTransaction("URL", "FailureReportPage");

		cat.logEvent("Error", OutOfMemoryError.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", RuntimeException.class.getName(), "ERROR", null);
		cat.logEvent("Exception", Exception.class.getName(), "ERROR", null);
		cat.logEvent("RuntimeException", NullPointerException.class.getName(), "ERROR", null);
		Thread.sleep(new Random().nextInt(10) * new Random().nextInt(3));
		t.setStatus("error");
		t.complete();
	}

	@Test
	public void demo2() throws Exception {
		MessageProducer cat = lookup(MessageProducer.class);
		Transaction t = cat.newTransaction("SQL3", "update-user");
		Thread.sleep(10);
		t.setStatus("error");
		t.complete();
	}
}
