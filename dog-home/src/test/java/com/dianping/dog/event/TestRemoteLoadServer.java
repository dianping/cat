package com.dianping.dog.event;

import org.junit.Test;

import com.dianping.dog.alarm.alert.AlertEventListener;
import com.dianping.dog.alarm.connector.DataService;
import com.dianping.dog.alarm.filter.ProblemFilterListener;
import com.dianping.dog.alarm.merge.ProblemMergeListener;
import com.site.lookup.ComponentTestCase;

public class TestRemoteLoadServer extends ComponentTestCase {

	@Test
	public void testRemoteLoadServer() throws Exception {
		EventListenerRegistry listenerRegistry = lookup(EventListenerRegistry.class);

		ProblemFilterListener problemFilterListener = lookup(ProblemFilterListener.class);
		listenerRegistry.register(problemFilterListener);

		ProblemMergeListener problemMergeListener = lookup(ProblemMergeListener.class);
		listenerRegistry.register(problemMergeListener);

		AlertEventListener alertEventListener = lookup(AlertEventListener.class);
		listenerRegistry.register(alertEventListener);

		DataService dataService = lookup(DataService.class);
		dataService.init();
		dataService.start();
		System.in.read();

	}
	
}
