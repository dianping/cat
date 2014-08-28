package com.dianping.cat.report.task.monitor.database;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;

public class TableCapacityService extends ContainerHolder implements Initializable, LogEnabled {

	private Map<String, CapacityUpdater> m_updaters = new HashMap<String, CapacityUpdater>();

	private static final double CAPACITY = 5;

	@Override
	public void enableLogging(Logger logger) {
	}

	@Override
	public void initialize() throws InitializationException {
		m_updaters = lookupMap(CapacityUpdater.class);
		Threads.forGroup("cat").start(new CapacityUpdateTask());
	}

	public class CapacityUpdateTask implements Task {

		@Override
		public String getName() {
			return "capacity-update-task";
		}

		@Override
		public void run() {
			for (CapacityUpdater updater : m_updaters.values()) {
				System.out.println(updater.getId());
				updater.updateCapacity(CAPACITY);
			}
		}

		@Override
		public void shutdown() {
		}

	}
}
