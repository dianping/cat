package com.dianping.cat.report.task.browser;

import java.util.Date;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.page.browser.task.WebDatabasePruner;
import com.dianping.cat.report.task.TaskBuilder;

public class WebDatabasePrunerTest  extends ComponentTestCase{
	private TaskBuilder m_appDatabasePruner;
	
	@Test
	public void testDao() {
		m_appDatabasePruner = lookup(TaskBuilder.class, WebDatabasePruner.ID);
		Date period = ((WebDatabasePruner) m_appDatabasePruner).queryPeriod(-3);

		try {
			((WebDatabasePruner) m_appDatabasePruner).pruneAjaxDataTable(period, 1);
			((WebDatabasePruner) m_appDatabasePruner).pruneSpeedDataTable(period,1);
			((WebDatabasePruner) m_appDatabasePruner).pruneJsLog(period);
		} catch (DalException e) {
			e.printStackTrace();
		}
	}
}
