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
package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.configuration.client.entity.Server;

@Named(type = TransportManager.class)
public class DefaultTransportManager implements TransportManager, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private TcpSocketSender m_tcpSocketSender;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public MessageSender getSender() {
		return m_tcpSocketSender;
	}

	@Override
	public void initialize() throws InitializationException {
		List<Server> servers = m_configManager.getServers();

		if (!m_configManager.isCatEnabled()) {
			m_tcpSocketSender = null;
			m_logger.warn("CAT was DISABLED due to not initialized yet!");
		} else {
			List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();

			for (Server server : servers) {
				if (server.isEnabled()) {
					addresses.add(new InetSocketAddress(server.getIp(), server.getPort()));
				}
			}

			m_logger.info("Remote CAT servers: " + addresses);

			if (addresses.isEmpty()) {
				throw new RuntimeException("All servers in configuration are disabled!\r\n" + servers);
			} else {
				m_tcpSocketSender.initialize(addresses);
			}
		}
	}

}
