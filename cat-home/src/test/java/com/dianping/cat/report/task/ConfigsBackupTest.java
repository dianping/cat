package com.dianping.cat.report.task;

import junit.framework.Assert;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.report.task.config.ConfigBackupTask;

public class ConfigsBackupTest extends ComponentTestCase {

	// @Test
	public void backupConfigsTest() {
		ConfigDao dao = lookup(ConfigDao.class);
		ConfigBackupTask task = new ConfigBackupTask(dao);

		Assert.assertTrue(task.backupConfigs());
	}
}
