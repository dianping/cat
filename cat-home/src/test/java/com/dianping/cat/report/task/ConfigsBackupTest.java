/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

		private static final String BASE_DIR_PATH = "src/main/resources/config/";

		private ConfigDao m_dao;

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
