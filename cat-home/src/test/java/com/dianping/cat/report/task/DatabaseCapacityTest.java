package com.dianping.cat.report.task;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.overload.task.CapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;

public class DatabaseCapacityTest extends ComponentTestCase {

	// @Test
	public void testTaskDuration() throws DalException {
		CapacityUpdater updater = lookup(CapacityUpdater.class, HourlyCapacityUpdater.ID);
		long currentMills = System.currentTimeMillis();

		updater.updateDBCapacity();
		System.out.println("Done: " + Long.toString(System.currentTimeMillis() - currentMills));
	}
}
