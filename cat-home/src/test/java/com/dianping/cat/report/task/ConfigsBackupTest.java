package com.dianping.cat.report.task;

import junit.framework.Assert;

import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.task.config.ConfigBackupTask;

public class ConfigsBackupTest extends ComponentTestCase {

	// @Test
	public void backupConfigsTest() {
		ConfigBackupTask task = lookup(ConfigBackupTask.class);

		Assert.assertTrue(task.backupConfigs());
	}
}
