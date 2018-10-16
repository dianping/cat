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
package com.dianping.cat.config.server;

import com.dianping.cat.configuration.server.entity.ConsumerConfig;
import com.dianping.cat.configuration.server.entity.LongConfig;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.StorageConfig;
import com.dianping.cat.configuration.server.transform.BaseVisitor;

public class ServerConfigVisitor extends BaseVisitor {

	private Server m_specificServer;

	public ServerConfigVisitor(Server server) {
		m_specificServer = server;
	}

	@Override
	public void visitConsumer(ConsumerConfig consumer) {
		ConsumerConfig c = m_specificServer.getConsumer();
		LongConfig l = null;

		if (c != null && (l = c.getLongConfig()) != null) {
			LongConfig longConfig = consumer.getLongConfig();

			longConfig.setDefaultServiceThreshold(l.getDefaultServiceThreshold());
			longConfig.setDefaultSqlThreshold(l.getDefaultSqlThreshold());
			longConfig.setDefaultUrlThreshold(l.getDefaultUrlThreshold());
			longConfig.getDomains().putAll(l.getDomains());
		}
	}

	@Override
	public void visitServer(Server server) {
		server.getProperties().putAll(m_specificServer.getProperties());
		super.visitServer(server);
	}

	@Override
	public void visitStorage(StorageConfig storage) {
		StorageConfig s = m_specificServer.getStorage();

		if (s != null) {
			storage.setHarMode(s.getHarMode()).setLocalBaseDir(s.getLocalBaseDir())
									.setLocalLogivewStorageTime(s.getLocalLogivewStorageTime());
			storage.setLocalReportStorageTime(s.getLocalReportStorageTime()).setMaxHdfsStorageTime(s.getMaxHdfsStorageTime())
									.setUploadThread(s.getUploadThread());

			storage.getHdfses().putAll(s.getHdfses());
			storage.getHarfses().putAll(s.getHarfses());
			storage.getProperties().putAll(s.getProperties());
		}
	}

}
