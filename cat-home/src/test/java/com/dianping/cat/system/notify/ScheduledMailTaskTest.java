package com.dianping.cat.system.notify;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

public class ScheduledMailTaskTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		Threads.forGroup("Cat").start(lookup(ScheduledMailTask.class));

		String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());

		System.out.println(String.format("[%s] [INFO] Press any key to stop server ... ", timestamp));
		System.in.read();
	}
}
