package com.dianping.cat.report.task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.thread.TaskProducer;

@RunWith(JUnit4.class)
public class TestTaskProducer extends ComponentTestCase {

   private TaskProducer m_taskProducer;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_taskProducer = lookup(TaskProducer.class);
	}

	@Test
	public void builderData() throws IOException {
		Threads.forGroup("Cat").start(m_taskProducer);
		
		String timestamp = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());

		System.out.println(String.format("[%s] [INFO] Press any key to stop server ... ", timestamp));
		System.in.read();
	}

}
