package com.dianping.cat.report.task.config;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.eunit.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class ConfigBackupTask {
	@Inject
	private ConfigDao m_dao;

	private static final String BASE_DIR_PATH = "src/main/resources/config/backup/";

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