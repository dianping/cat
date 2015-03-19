package com.dianping.cat.report.task.app;

import java.util.Date;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.app.task.AppDatabasePruner;
import com.dianping.cat.report.task.TaskBuilder;

public class AppDatabasePrunerTest extends ComponentTestCase {

	private TaskBuilder m_appDatabasePruner;

	@Test
	public void testDao() {
		m_appDatabasePruner = lookup(TaskBuilder.class, AppDatabasePruner.ID);
		Date period = ((AppDatabasePruner) m_appDatabasePruner).queryPeriod(-1);

		try {
			((AppDatabasePruner) m_appDatabasePruner).pruneAppCommandTable(period, 1);
			((AppDatabasePruner) m_appDatabasePruner).pruneAppSpeedTable(period, 1);
		} catch (DalException e) {
			e.printStackTrace();
		}
	}
}
