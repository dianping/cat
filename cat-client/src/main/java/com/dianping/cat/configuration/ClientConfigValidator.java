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
package com.dianping.cat.configuration;

import java.text.MessageFormat;
import java.util.Date;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultValidator;

public class ClientConfigValidator extends DefaultValidator {
	private ClientConfig m_config;

	private String getLocalAddress() {
		return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	private void log(String severity, String message) {
		MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");

		System.out.println(format.format(new Object[] { new Date(), severity, "cat", message }));
	}

	@Override
	public void visitConfig(ClientConfig config) {
		config.setMode("client");

		if (config.getServers().size() == 0) {
			config.setEnabled(false);
			log("WARN", "CAT client was disabled due to no CAT servers configured!");
		} else if (!config.isEnabled()) {
			log("WARN", "CAT client was globally disabled!");
		}

		m_config = config;
		super.visitConfig(config);

		if (m_config.isEnabled()) {
			for (Domain domain : m_config.getDomains().values()) {
				if (!domain.isEnabled()) {
					m_config.setEnabled(false);
					log("WARN", "CAT client was disabled in domain(" + domain.getId() + ") explicitly!");
				}

				break; // for first domain only
			}
		}
	}

	@Override
	public void visitDomain(Domain domain) {
		super.visitDomain(domain);

		// set default values
		if (domain.getEnabled() == null) {
			domain.setEnabled(true);
		}

		if (domain.getIp() == null) {
			domain.setIp(getLocalAddress());
		}
	}

	@Override
	public void visitServer(Server server) {
		super.visitServer(server);

		// set default values
		if (server.getPort() == null) {
			server.setPort(2280);
		}

		if (server.getEnabled() == null) {
			server.setEnabled(true);
		}
	}

}
