package com.dianping.dog.event;

import org.junit.Test;

import com.dianping.dog.notify.job.StandardScheduleJobRunner;
import com.site.lookup.ComponentTestCase;

public class TestNotifyServer extends ComponentTestCase {
	
	@Test
	public void testNotifyServer() throws Exception{
		StandardScheduleJobRunner jobRunner = lookup(StandardScheduleJobRunner.class);
		
		jobRunner.start();
		System.in.read();
	}

}
