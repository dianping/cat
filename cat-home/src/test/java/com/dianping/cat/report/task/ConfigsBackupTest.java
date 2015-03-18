package com.dianping.cat.report.task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class ConfigsBackupTest extends ComponentTestCase {

	@Test
	public void backupConfigsTest() {
		ConfigDao dao = lookup(ConfigDao.class);
		ConfigBackupTask task = new ConfigBackupTask(dao);

		Assert.assertTrue(task.backupConfigs());
	}

	public class ConfigBackupTask {

		private ConfigDao m_dao;

		private static final String BASE_DIR_PATH = "src/main/resources/config/";

		public ConfigBackupTask(ConfigDao dao) {
			m_dao = dao;
		}

		private boolean backupConfig(String name, String context) {
			String filePath = BASE_DIR_PATH + name + ".xml";
			File backupFile = new File(filePath);

			try {
				Files.forIO().writeTo(backupFile, context);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

		public boolean backupConfigs() {
			boolean result = true;

			try {
				List<Config> configs = m_dao.findAllConfig(ConfigEntity.READSET_FULL);

				for (Config config : configs) {
					boolean tmpResult = backupConfig(config.getName(), config.getContent());

					if (!tmpResult && result) {
						result = false;
					}
				}
			} catch (DalException e) {
				return false;
			}
			return result;
		}
	}
}
